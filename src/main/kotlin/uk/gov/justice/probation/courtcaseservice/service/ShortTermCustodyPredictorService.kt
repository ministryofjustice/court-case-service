package uk.gov.justice.probation.courtcaseservice.service

import hex.genmodel.easy.EasyPredictModelWrapper
import hex.genmodel.easy.RowData
import hex.genmodel.easy.exception.PredictUnknownCategoricalLevelException
import hex.genmodel.easy.prediction.BinomialModelPrediction
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.ManageOffencesRestClient
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiEntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiUnknownException
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Period

@Service
class ShortTermCustodyPredictorService(
    private val model: EasyPredictModelWrapper,
    private val offencesRestClient: ManageOffencesRestClient
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)

        fun isMagistratesCourtCode(courtCode: String) : Boolean {
            return courtCode.startsWith("B")
        }
    }

    fun calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters: ShortTermCustodyPredictorParameters) : Double? {

        log.debug("Entered calculateShortTermCustodyPredictorScore(${shortTermCustodyPredictorParameters.courtCode}, " +
                "${shortTermCustodyPredictorParameters.offenderAge}, " +
                "${shortTermCustodyPredictorParameters.offenceCode})")

        val rowData = RowData()

        rowData["court_code"] = shortTermCustodyPredictorParameters.courtCode
        shortTermCustodyPredictorParameters.offenderAge?.let {
            rowData["offender_age"] = shortTermCustodyPredictorParameters.offenderAge.toString()
        }
        rowData["offence_code"] = shortTermCustodyPredictorParameters.offenceCode.replace("/", StringUtils.EMPTY)

        val prediction : BinomialModelPrediction? =
        try {
            model.predictBinomial(rowData)
        }
        catch (ex: PredictUnknownCategoricalLevelException) {
            log.warn("Invalid parameter passed into algorithm", ex)
            null
        }

        prediction?.let {
            val score = prediction.classProbabilities[0]
            log.info("Calculated short term custody score of $score for court code: ${shortTermCustodyPredictorParameters.courtCode}, offender age: ${shortTermCustodyPredictorParameters.offenderAge}, offence code: ${shortTermCustodyPredictorParameters.offenceCode}")
            return score
        }

        return null
    }

    fun addPredictorScoresToHearing(hearingEntity: HearingEntity) {
        log.debug("Entered addPredictorScoresToHearing for hearing with case number: ${hearingEntity.caseNo}")

        if (!isMagistratesCourtCode(hearingEntity.hearingDays[0].courtCode)) {
            log.info("Short term custody algorithm only support magistrate court hearings - no score will be calculated")
            return
        }

        log.debug("Case has ${hearingEntity.hearingDefendants.size} defendants")
        hearingEntity.hearingDefendants.forEach { defendant ->
            log.debug("Defendant has ${defendant.offences.size} offences")
            defendant.offences.forEach { offence ->
                val homeOfficeOffenceCode : String? =
                try {
                    offence.offenceCode?.let { offencesRestClient.getHomeOfficeOffenceCodeByCJSCode(it) }
                } catch (e : ExternalApiEntityNotFoundException) {
                    log.warn("No corresponding Home office offence code could be found for CJS code: ${offence.offenceCode}", e)
                    null
                } catch (e : ExternalApiUnknownException) {
                    log.error("Unknown error occurred whilst looking up Home office offence code for CJS code: ${offence.offenceCode}", e)
                    null
                }
                log.debug("Home office code returned: $homeOfficeOffenceCode")
                addPredictorScoreToOffence(homeOfficeOffenceCode, hearingEntity, defendant, offence)
            }
        }
    }

    private fun addPredictorScoreToOffence(
        homeOfficeOffenceCode: String?,
        hearingEntity: HearingEntity,
        defendant: HearingDefendantEntity?,
        offence: OffenceEntity
    ) {
        if (homeOfficeOffenceCode != null) {
            log.debug("Found Home office offence code: $homeOfficeOffenceCode")

            val score = calculateShortTermCustodyPredictorScore(
                ShortTermCustodyPredictorParameters(
                    calculateAge(defendant),
                    homeOfficeOffenceCode,
                    hearingEntity.hearingDays[0].courtCode
                )
            )
            score?.let {
                log.debug("Updating offence with short term custody predictor score of $score")
                offence.shortTermCustodyPredictorScore = BigDecimal.valueOf(score)
            }
        }
    }

    private fun calculateAge(defendantEntity: HearingDefendantEntity?): Int? {
        defendantEntity?.defendant?.dateOfBirth?.let {
            return Period.between(it, LocalDate.now()).years
        }
        log.warn("Could not calculate defendant age as no DOB provided")
        return null
    }


}