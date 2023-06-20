package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException

@Service
class CaseWorkflowService(val hearingRepository: HearingRepository, val courtRepository: CourtRepository) {

    fun addHearingOutcome(hearingId: String, hearingOutcomeType: HearingOutcomeType) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
            { hearingEntity: HearingEntity ->
                hearingEntity.addHearingOutcome(hearingOutcomeType)
                hearingRepository.save(hearingEntity)
            },
            {
                throw EntityNotFoundException("Hearing not found with id $hearingId")
            })
    }

    fun fetchHearingOutcomes(courtCode: String, state: HearingOutcomeItemState): List<HearingOutcomeResponse> {
        courtRepository.findByCourtCode(courtCode)
            .orElseThrow {
                EntityNotFoundException(
                    "Court %s not found",
                    courtCode
                )
            }
        return hearingRepository.findByCourtCodeAndHearingOutcome(courtCode, state).flatMap { HearingOutcomeResponse.of(it) };
    }
}