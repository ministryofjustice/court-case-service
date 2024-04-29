package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingDefendantOutcomesRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeAssignedUser
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCountByState as V2HearingOutcomeCountByState
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCaseList as V2HearingOutcomeCaseList
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


@Service
class CaseWorkflowService(val hearingRepository: HearingRepository,
                          val courtRepository: CourtRepository,
                          val hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom,
                          val telemetryService: TelemetryService,
                          @Value("\${hearing_outcomes.move_un_resulted_to_outcomes_courts:}")
                          val courtCodes: List<String> = listOf(),
                          @Value("\${hearing_outcomes.move_un_resulted_to_outcomes_cutoff_time:18:30}")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                          val cutOffTime: LocalTime = LocalTime.of(18, 30)) {

    fun addOrUpdateHearingOutcome(hearingId: String, defendantId: String, hearingOutcomeType: HearingOutcomeType) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
            { hearingEntity: HearingEntity ->
                val hearingDefendant = hearingEntity.getHearingDefendant(defendantId)
                    ?: throw EntityNotFoundException("Defendant $defendantId not found on hearing with id $hearingId")
                if (hearingDefendant.hearingOutcome == null) {
                    hearingDefendant.addHearingOutcome(hearingOutcomeType)
                } else {
                    hearingDefendant.hearingOutcome.update(hearingOutcomeType)
                }
                hearingRepository.save(hearingEntity)
            },
            {
                throw EntityNotFoundException("Hearing not found with id $hearingId")
            })
    }

    fun assignAndUpdateStateToInProgress(hearingId: String, defendantId: String, assignedTo: String, assignedToUuid: String) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
                {
                    val hearingDefendant = it.getHearingDefendant(defendantId)
                        ?: throw EntityNotFoundException("Defendant $defendantId not found on hearing with id $hearingId")

                    hearingDefendant.hearingOutcome.assignTo(assignedTo, assignedToUuid)
                    hearingRepository.save(it)
                },
                {
                    throw EntityNotFoundException("Hearing not found with id $hearingId")
                })
    }

    fun resultHearingOutcome(hearingId: String, defendantId: String, userUuid: String) {
        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
                {
                    val hearingDefendant = it.getHearingDefendant(defendantId)
                        ?: throw EntityNotFoundException("Defendant $defendantId not found on hearing with id $hearingId")

                    val hearingOutcome = hearingDefendant.hearingOutcome
                    if(hearingOutcome.assignedToUuid != userUuid) {
                        throw ForbiddenException("Outcome not allocated to current user.")
                    }
                    if(hearingOutcome.state != HearingOutcomeItemState.IN_PROGRESS.name) {
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state for outcome to be resulted.")
                    }
                    hearingOutcome.state = HearingOutcomeItemState.RESULTED.name
                    hearingOutcome.resultedDate = LocalDateTime.now()
                    hearingRepository.save(it)
                },
                {
                    throw EntityNotFoundException("Hearing not found with id $hearingId")
                })
    }

    fun fetchHearingOutcomes(courtCode: String, hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): HearingOutcomeCaseList {
        checkCourtExists(courtCode)

        val hearingOutcomes: List<HearingOutcomeResponse> = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(
            courtCode,
            hearingOutcomeSearchRequest
        ).map { HearingOutcomeResponse.of(it.first, it.second) }

        val page: PageImpl<HearingOutcomeResponse> = getPageableHearingOutcomes(hearingOutcomes, hearingOutcomeSearchRequest)

        return HearingOutcomeCaseList(
            page.content,
            getOutcomeCountsByState(courtCode),
            hearingRepository.getCourtroomsForCourt(courtCode),
            page.totalPages,
            hearingOutcomeSearchRequest.page,
            page.totalElements.toInt(),
            allAssignedUsers(hearingOutcomes)
        )
    }

    fun fetchV2HearingDefendantOutcomes(courtCode: String, searchRequest: HearingDefendantOutcomesRequest): V2HearingOutcomeCaseList {
        checkCourtExists(courtCode)
        val v1HearingOutcomeSearchRequest = HearingOutcomeSearchRequest(searchRequest.state, searchRequest.outcomeTypes, searchRequest.sortBy, searchRequest.order, searchRequest.courtRooms, searchRequest.assignedUsers, searchRequest.page, searchRequest.size)

        val hearingOutcomes: List<HearingOutcomeResponse> = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(
            courtCode,
            v1HearingOutcomeSearchRequest
        ).map { HearingOutcomeResponse.of(it.first, it.second) }

        val page: PageImpl<HearingOutcomeResponse> = getPageableHearingOutcomes(hearingOutcomes, v1HearingOutcomeSearchRequest)

        return V2HearingOutcomeCaseList(
            page.content,
            getV2OutcomeCountsByState(courtCode),
            hearingRepository.getCourtroomsForCourt(courtCode),
            page.totalPages,
            v1HearingOutcomeSearchRequest.page,
            page.totalElements.toInt(),
            allAssignedUsers(hearingOutcomes)
        )
    }

    private fun checkCourtExists(courtCode: String){
        courtRepository.findByCourtCode(courtCode)
            .orElseThrow {
                EntityNotFoundException(
                    "Court %s not found",
                    courtCode
                )
            }
    }

    fun getPageableHearingOutcomes(hearingOutcomes: List<HearingOutcomeResponse>, hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): PageImpl<HearingOutcomeResponse>{
        val pageRequest: Pageable = PageRequest.of(hearingOutcomeSearchRequest.page - 1, hearingOutcomeSearchRequest.size);
        var start = pageRequest.offset.toInt()
        val end = (start + pageRequest.pageSize).coerceAtMost(hearingOutcomes.size)

        val pageContent: List<HearingOutcomeResponse>? = hearingOutcomes?.subList(start, end)
        return PageImpl<HearingOutcomeResponse>(pageContent, pageRequest, hearingOutcomes.size.toLong())
    }

    fun allAssignedUsers(hearingOutcomes: List<HearingOutcomeResponse>): List<HearingOutcomeAssignedUser> {
        return hearingOutcomes.filter { hearingOutcomes -> !hearingOutcomes.assignedTo.isNullOrEmpty() }
            .map{ hearingOutcome -> HearingOutcomeAssignedUser(hearingOutcome.assignedTo, hearingOutcome.assignedToUuid) }
    }

    fun getOutcomeCountsByState(courtCode: String): HearingOutcomeCountByState {
        val dynamicOutcomeCountsByState = hearingOutcomeRepositoryCustom.getDynamicOutcomeCountsByState(courtCode)
        return HearingOutcomeCountByState(
            dynamicOutcomeCountsByState[HearingOutcomeItemState.NEW.name] ?: 0,
            dynamicOutcomeCountsByState[HearingOutcomeItemState.IN_PROGRESS.name] ?: 0,
            dynamicOutcomeCountsByState[HearingOutcomeItemState.RESULTED.name] ?: 0
        )
    }

    fun getV2OutcomeCountsByState(courtCode: String): V2HearingOutcomeCountByState {
        val dynamicOutcomeCountsByState = hearingOutcomeRepositoryCustom.getDynamicOutcomeCountsByState(courtCode)
        return V2HearingOutcomeCountByState(
            listOf(
                Pair(
                    HearingOutcomeItemState.NEW.name,
                    dynamicOutcomeCountsByState[HearingOutcomeItemState.NEW.name] ?: 0
                ),
                Pair(
                    HearingOutcomeItemState.IN_PROGRESS.name,
                    dynamicOutcomeCountsByState[HearingOutcomeItemState.IN_PROGRESS.name] ?: 0
                ),
                Pair(
                    HearingOutcomeItemState.RESULTED.name,
                    dynamicOutcomeCountsByState[HearingOutcomeItemState.RESULTED.name] ?: 0
                )
            )
        )
    }

    @Transactional
    fun processUnResultedCases() {

        var count: Optional<Int> = Optional.ofNullable(0)

        try {
            if(LocalTime.now().isBefore(cutOffTime)) {
                throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invoked before cutoff time: $cutOffTime")
            }

            count = if (this.courtCodes.isEmpty()) {
                hearingRepository.moveUnResultedCasesToOutcomesWorkflow()
            } else {
                hearingRepository.moveUnResultedCasesToOutcomesWorkflow(courtCodes)
            }

            telemetryService.trackMoveUnResultedCasesToOutcomesFlowJob(count.get(), courtCodes, null)
        } catch (e: Exception) {
            telemetryService.trackMoveUnResultedCasesToOutcomesFlowJob(count.get(), courtCodes, e)
            throw e
        }
    }

    fun updatePrepStatus(hearingId: String, defendantId: String, prepStatus: HearingPrepStatus) {

        hearingRepository.findFirstByHearingId(hearingId).ifPresentOrElse(
            {
                val hearingDefendant = it.getHearingDefendant(defendantId)
                    ?: throw EntityNotFoundException("Defendant $defendantId not found on hearing with id $hearingId")
                hearingDefendant.prepStatus = prepStatus.name
                hearingRepository.save(it)
            },
            {
                throw EntityNotFoundException("Hearing not found with id $hearingId")
            })
    }
}