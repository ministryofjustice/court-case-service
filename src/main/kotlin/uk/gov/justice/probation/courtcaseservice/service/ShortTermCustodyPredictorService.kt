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
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Period

@Service
class ShortTermCustodyPredictorService(
    private val model: EasyPredictModelWrapper,
    private val offencesRestClient: ManageOffencesRestClient,
    private val courtRepository: CourtRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters: ShortTermCustodyPredictorParameters) : Double? {

        log.debug("Entered calculateShortTermCustodyPredictorScore(${shortTermCustodyPredictorParameters.courtName}, " +
                "${shortTermCustodyPredictorParameters.offenderAge}, " +
                "${shortTermCustodyPredictorParameters.offenceCode})")

        val rowData = RowData()

        rowData["court_name"] = shortTermCustodyPredictorParameters.courtName
            .replace(".", StringUtils.EMPTY)
            .replace("'", StringUtils.EMPTY)
        shortTermCustodyPredictorParameters.offenderAge?.let {
            rowData["offender_age"] = shortTermCustodyPredictorParameters.offenderAge.toString()
        }
        rowData["offence_code"] = shortTermCustodyPredictorParameters.offenceCode.replace("/", StringUtils.EMPTY)

        var prediction : BinomialModelPrediction? =
        try {
            model.predictBinomial(rowData)
        }
        catch (ex: PredictUnknownCategoricalLevelException) {
            log.warn("Invalid parameter passed into algorithm", ex)
            null
        }

        prediction?.let {
            val score = prediction.classProbabilities[0]
            log.info("Calculated short term custody score of $score for court: ${shortTermCustodyPredictorParameters.courtName}, offender age: ${shortTermCustodyPredictorParameters.offenderAge}, offence code: ${shortTermCustodyPredictorParameters.offenceCode}")
            return score
        }

        return null
    }

    fun addPredictorScoresToHearing(hearingEntity: HearingEntity) {
        log.debug("Entered addPredictorScoresToHearing for hearing with case number: ${hearingEntity.caseNo}")

        hearingEntity.hearingDefendants.forEach { defendant ->
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
            courtRepository.findByCourtCode(hearingEntity.hearingDays[0].courtCode).ifPresentOrElse({
                val score = calculateShortTermCustodyPredictorScore(
                    ShortTermCustodyPredictorParameters(
                        it.name,
                        calculateAge(defendant),
                        homeOfficeOffenceCode
                    )
                )
                score?.let {
                    offence.shortTermCustodyPredictorScore = BigDecimal.valueOf(score)
                }
            }, {
                log.warn("No court name could be found for court code: ${hearingEntity.hearingDays[0].courtCode}")
            })
        }
    }

    private fun calculateAge(defendantEntity: HearingDefendantEntity?): Int? {
        return Period.between(defendantEntity?.defendant?.dateOfBirth, LocalDate.now()).years
    }
}