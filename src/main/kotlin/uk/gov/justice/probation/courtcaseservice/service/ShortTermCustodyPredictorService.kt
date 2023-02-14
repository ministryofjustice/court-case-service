package uk.gov.justice.probation.courtcaseservice.service

import hex.genmodel.easy.EasyPredictModelWrapper
import hex.genmodel.easy.RowData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.ManageOffencesRestClient
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiEntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiUnknownException
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
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

    fun calculateShortTermCustodyPredictorScore(predictorParameters: PredictorParameters) : Double {

        log.debug("Entered calculateShortTermCustodyPredictorScore(${predictorParameters.courtName}, " +
                "${predictorParameters.offenderAge}, " +
                "${predictorParameters.offenceCode})")

        val rowData = RowData()

        rowData["court_name"] = predictorParameters.courtName
        predictorParameters.offenderAge?.let {
            rowData["offender_age"] = predictorParameters.offenderAge.toString()
        }
        rowData["offence_code"] = predictorParameters.offenceCode

        val prediction = model.predictBinomial(rowData)
        val score = prediction.classProbabilities[0]
        log.info("Calculated short term custody score of $score for court: ${predictorParameters.courtName}, offender age: ${predictorParameters.offenderAge}, offence code: ${predictorParameters.offenceCode}")

        return score
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

                if (homeOfficeOffenceCode != null) {
                    log.debug("Found Home office offence code: $homeOfficeOffenceCode")
                    courtRepository.findByCourtCode(hearingEntity.hearingDays[0].courtCode).ifPresentOrElse({
                        val score = calculateShortTermCustodyPredictorScore(
                            PredictorParameters(
                                it.name,
                                calculateAge(defendant),
                                homeOfficeOffenceCode
                            )
                        )
                        offence.shortTermCustodyPredictorScore = BigDecimal.valueOf(score)
                    }, {
                        log.warn("No court name could be found for court code: ${hearingEntity.hearingDays[0].courtCode}")
                    })
                }
            }
        }
    }

    private fun calculateAge(defendantEntity: HearingDefendantEntity?): Int? {
        return Period.between(defendantEntity?.defendant?.dateOfBirth, LocalDate.now()).years
    }
}