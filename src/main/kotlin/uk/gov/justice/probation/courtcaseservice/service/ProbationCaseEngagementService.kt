package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusOffenderDetail
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository

@Service
class ProbationCaseEngagementService (val defendantRepository: DefendantRepository,
                                      val telemetryService: TelemetryService){
    private companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun updateMatchingDefendantsWithCrn(deliusOffenderDetail: DeliusOffenderDetail?) {
        log.debug("Mapping new offender with details {}", deliusOffenderDetail)
        defendantRepository.findMatchingDefendants(
            deliusOffenderDetail?.identifiers?.pnc,
            deliusOffenderDetail?.dateOfBirth,
            deliusOffenderDetail?.name?.forename,
            deliusOffenderDetail?.name?.surname
        ).forEach { defendantEntity ->
            updateDefendant(defendantEntity, deliusOffenderDetail)
        }
    }

    private fun updateDefendant(
        defendantEntity: DefendantEntity,
        deliusOffenderDetail: DeliusOffenderDetail?
    ) {
        log.debug("Updating defendant {} ", defendantEntity.defendantId)
        deliusOffenderDetail?.identifiers?.crn.also { defendantEntity.crn = it }
        val updatedDefendant = defendantRepository.save(defendantEntity)
        telemetryService.trackPiCNewEngagementDefendantLinkedEvent(updatedDefendant)
    }
}