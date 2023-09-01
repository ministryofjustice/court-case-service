package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException

@Service
class CaseWorkflowService(val hearingRepository: HearingRepository, val courtRepository: CourtRepository, val hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom) {

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

    fun assignAndUpdateStateToInProgress(hearingId: String, assignedTo: String, assignedToUuid: String) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
                { hearingEntity: HearingEntity ->
                    hearingEntity.hearingOutcome.assignTo(assignedTo, assignedToUuid)
                    hearingRepository.save(hearingEntity)
                },
                {
                    throw EntityNotFoundException("Hearing not found with id $hearingId")
                })
    }

    fun fetchHearingOutcomes(courtCode: String, hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): List<HearingOutcomeResponse> {
        courtRepository.findByCourtCode(courtCode)
            .orElseThrow {
                EntityNotFoundException(
                    "Court %s not found",
                    courtCode
                )
            }
        return hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome2(courtCode, hearingOutcomeSearchRequest).flatMap { HearingOutcomeResponse.of(it.first, it.second) }
    }
}