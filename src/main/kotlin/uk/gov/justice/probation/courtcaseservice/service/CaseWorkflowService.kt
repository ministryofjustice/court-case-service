package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException

@Service
class CaseWorkflowService(val hearingRepository: HearingRepository, val courtRepository: CourtRepository, val hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom) {

    fun addOrUpdateHearingOutcome(hearingId: String, hearingOutcomeType: HearingOutcomeType) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
            { hearingEntity: HearingEntity ->
                if (hearingEntity.hearingOutcome == null) {
                    hearingEntity.addHearingOutcome(hearingOutcomeType)
                } else {
                    hearingEntity.hearingOutcome.update(hearingOutcomeType)
                }
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

    fun resultHearingOutcome(hearingId: String, userUuid: String) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
                {
                    if(it.hearingOutcome.assignedToUuid != userUuid) {
                        throw ForbiddenException("Outcome not allocated to current user.")
                    }
                    if(it.hearingOutcome.state != HearingOutcomeItemState.IN_PROGRESS.name) {
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state for outcome to be resulted.")
                    }
                    it.hearingOutcome.state = HearingOutcomeItemState.RESULTED.name
                    hearingRepository.save(it)
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
        return hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(courtCode, hearingOutcomeSearchRequest).flatMap { HearingOutcomeResponse.of(it.first, it.second) }
    }
}