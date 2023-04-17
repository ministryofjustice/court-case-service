package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException

@Service
class CaseWorkflowService(val hearingRepository: HearingRepository) {

    fun addHearingOutcome(hearingId: String, hearingOutcomeType: HearingOutcomeType) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
            { hearingEntity: HearingEntity ->
                hearingEntity.addHearingOutcome(hearingOutcomeType)
                hearingRepository.save(hearingEntity)
            },
            {
                throw EntityNotFoundException(
                    "Hearing not found with id %s",
                    hearingId
                )
            })
    }
}