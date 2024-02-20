package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowServiceTest {

    companion object {
        val TEST_COURT_ROOMS = listOf("01", "Court room - 2")
    }

    @Mock
    lateinit var hearingRepository: HearingRepository

    @Mock
    lateinit var hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom

    @Mock
    lateinit var courtRepository: CourtRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    lateinit var caseWorkflowService: CaseWorkflowService

    @Captor
    lateinit var hearingEntityCaptor: ArgumentCaptor<HearingEntity>
    @BeforeEach
    fun initTest() {
         caseWorkflowService = CaseWorkflowService(hearingRepository, courtRepository, hearingOutcomeRepositoryCustom, telemetryService)
    }

    @Test
    fun `given hearing outcome and hearing id exist should add hearing outcome`() {
        val hearingId = "hearing-id-one"
        val dbHearingEntity = HearingEntity.builder().build()
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(dbHearingEntity))
        caseWorkflowService.addOrUpdateHearingOutcome(hearingId, HearingOutcomeType.REPORT_REQUESTED)
        verify(hearingRepository).findFirstByHearingId(hearingId)
        assertThat(dbHearingEntity.hearingOutcome)
            .isEqualTo(HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").build())
    }

    @Test
    fun `given hearing outcome and hearing outcome exists, should update hearing outcome`() {
        val hearingId = "hearing-id-one"
        val dbHearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.ADJOURNED.name).build())
            .build()
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(dbHearingEntity))
        caseWorkflowService.addOrUpdateHearingOutcome(hearingId, HearingOutcomeType.REPORT_REQUESTED)
        verify(hearingRepository).findFirstByHearingId(hearingId)
        assertThat(dbHearingEntity.hearingOutcome)
            .isEqualTo(HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").build())
    }

    @Test
    fun `given hearing outcome and hearing id does not exist should throw entity not found exception`() {
        val hearingId = "hearing-id-one"
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.empty())
        assertThrows(
            "Hearing not found with id hearing-id-one",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.addOrUpdateHearingOutcome(
                hearingId,
                HearingOutcomeType.REPORT_REQUESTED
            )
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
    }

    @Test
    fun `should update hearing outcome with assigned to user details`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedTo = "John Smith"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
                .hearingOutcome(HearingOutcomeEntity.builder().build())
                .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        caseWorkflowService.assignAndUpdateStateToInProgress(hearingId, assignedTo, assignedToUuid)

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository).save(hearingEntityCaptor.capture())

        val entity = hearingEntityCaptor.value
        assertThat(entity.hearingOutcome.state).isEqualTo(HearingOutcomeItemState.IN_PROGRESS.name)
        assertThat(entity.hearingOutcome.assignedTo).isEqualTo(assignedTo)
        assertThat(entity.hearingOutcome.assignedToUuid).isEqualTo(assignedToUuid)
    }

    @Test
    fun `should throw entity not found exception when hearing does not exsist`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedTo = "John Smith"
        val assignedToUuid = "test-uuid"

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.empty())

        // When
        assertThrows(
                "Hearing not found with id hearing-id-one",
                EntityNotFoundException::class.java
        ) {

            caseWorkflowService.assignAndUpdateStateToInProgress(
                    hearingId,
                    assignedTo,
                    assignedToUuid
            )
        }

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository, never()).save(any())
    }

    @Test
    fun `given court code and outcome type filter invoke repository and return hearing outcomes`() {
        val hearingOutcomeEntity1 = HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.REPORT_REQUESTED.name).outcomeDate(
            LocalDateTime.of(2023, 6,6, 19, 9, 1)).state("NEW").build()
        val hearingOutcomeEntity2 = HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.ADJOURNED.name).outcomeDate(
            LocalDateTime.of(2023, 5,5, 19, 9, 5)).state("NEW").build()

        val hearing1 = EntityHelper.aHearingEntityWithHearingId("case-id-1", "hearing-id-1", "defendant-id-1").withHearingOutcome(hearingOutcomeEntity1)
        val hearing2 = EntityHelper.aHearingEntityWithHearingId("case-id-2", "hearing-id-2", "defendant-id-2").withHearingOutcome(hearingOutcomeEntity2)

        given(courtRepository.findByCourtCode(COURT_CODE)).willReturn(Optional.of(CourtEntity.builder().build()))
        given(
            hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(
                COURT_CODE,
                HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW)
            )
        ).willReturn(
            PageImpl(
                listOf(
                    Pair<HearingEntity, LocalDate>(hearing1, EntityHelper.SESSION_START_TIME.toLocalDate()),
                    Pair<HearingEntity, LocalDate>(hearing2, EntityHelper.SESSION_START_TIME.toLocalDate())
                ),
                Pageable.ofSize(2),
                9
            )
        )

        given(hearingRepository.getCourtroomsForCourt(EntityHelper.COURT_CODE)).willReturn(TEST_COURT_ROOMS)

        val hearingOutcomes = caseWorkflowService.fetchHearingOutcomes(COURT_CODE, HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW))

        assertThat(hearingOutcomes).isEqualTo(
            HearingOutcomeCaseList(
                listOf(
                    HearingOutcomeResponse(
                        hearingOutcomeType = HearingOutcomeType.REPORT_REQUESTED,
                        outcomeDate = LocalDateTime.of(2023, 6, 6, 19, 9, 1),
                        hearingDate = EntityHelper.SESSION_START_TIME.toLocalDate(),
                        hearingId = "hearing-id-1",
                        defendantId = "defendant-id-1",
                        probationStatus = EntityHelper.PROBATION_STATUS,
                        offences = listOf(EntityHelper.OFFENCE_TITLE),
                        defendantName = EntityHelper.DEFENDANT_NAME,
                        crn = "X340906",
                        state = HearingOutcomeItemState.NEW
                    ),
                    HearingOutcomeResponse(
                        hearingOutcomeType = HearingOutcomeType.ADJOURNED,
                        outcomeDate = LocalDateTime.of(2023, 5, 5, 19, 9, 5),
                        hearingDate = EntityHelper.SESSION_START_TIME.toLocalDate(),
                        hearingId = "hearing-id-2",
                        defendantId = "defendant-id-2",
                        probationStatus = EntityHelper.PROBATION_STATUS,
                        offences = listOf(EntityHelper.OFFENCE_TITLE),
                        defendantName = EntityHelper.DEFENDANT_NAME,
                        crn = "X340906",
                        state = HearingOutcomeItemState.NEW
                    )
                ),
                hearingOutcomes.countsByState,
                TEST_COURT_ROOMS,
                        5,
                        1,
                        9
            )
        )
    }

    @Test
    fun `given non existing court code when get hearing outcomes then throw entity not found `() {
        given(courtRepository.findByCourtCode(COURT_CODE)).willReturn(Optional.empty())
        assertThrows(
            "Court B10JQ not found",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.fetchHearingOutcomes(
                COURT_CODE,
                HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW)
            )
        }
        verifyNoInteractions(hearingRepository)
    }

    @Test
    fun `given existing hearing with outcome in IN_PROGRESS and allocated to current user, should mark outcome as RESULTED`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        caseWorkflowService.resultHearingOutcome(hearingId, assignedToUuid)

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository).save(hearingEntityCaptor.capture())

        assertThat(hearingEntityCaptor.value.hearingOutcome.state).isEqualTo(HearingOutcomeItemState.RESULTED.name)
        assertThat(hearingEntityCaptor.value.hearingOutcome.resultedDate).isNotNull()
    }

    @Test
    fun `given existing hearing with outcome in IN_PROGRESS and NOT allocated to current user, should throw forbidden error`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Outcome not allocated to current user.",
            ForbiddenException::class.java
        ) {
            caseWorkflowService.resultHearingOutcome(hearingId, "un-allocated-to-user")
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verifyNoMoreInteractions(hearingRepository)
    }

    @Test
    fun `given existing hearing with outcome NOT in IN_PROGRESS state, should throw bad request error`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.NEW.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Invalid state for outcome to be resulted.",
            ResponseStatusException::class.java
        ) {
            caseWorkflowService.resultHearingOutcome(hearingId, assignedToUuid)
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verifyNoMoreInteractions(hearingRepository)
    }

    @Test
    fun `should throw entity not found exception when hearing does not match a valid hearing id when attempting to place on hold`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.empty())
        // When
        assertThrows(
            "Hearing not found with id hearing-id-one",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.holdHearingOutcome(hearingId, assignedToUuid)
        }
        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository, never()).save(any())
    }
    @Test
    fun `given existing hearing with outcome in IN_PROGRESS and allocated to current user, should mark outcome as ON_HOLD`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        caseWorkflowService.holdHearingOutcome(hearingId, assignedToUuid)

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository).save(hearingEntityCaptor.capture())

        assertThat(hearingEntityCaptor.value.hearingOutcome.state).isEqualTo(HearingOutcomeItemState.ON_HOLD.name)
    }

    @Test
    fun `given existing hearing with outcome in IN_PROGRESS and NOT allocated to current user, should throw forbidden error when attempting to place on hold`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Outcome not allocated to current user.",
            ForbiddenException::class.java
        ) {
            caseWorkflowService.holdHearingOutcome(hearingId, "un-allocated-to-user")
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verifyNoMoreInteractions(hearingRepository)
    }

    @Test
    fun `given existing hearing with outcome NOT in IN_PROGRESS state, should throw bad request error when attempting to place on hold`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.NEW.name).assignedToUuid(assignedToUuid).build())
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Invalid state for outcome to be resulted.",
            ResponseStatusException::class.java
        ) {
            caseWorkflowService.holdHearingOutcome(hearingId, assignedToUuid)
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verifyNoMoreInteractions(hearingRepository)
    }

    @Test
    fun `given court code, when get count by state, should invoke repository and return count`() {
        val courtCode = "B10JQ"
        given(hearingOutcomeRepositoryCustom.getDynamicOutcomeCountsByState(courtCode)).willReturn(mapOf("NEW" to 2, "RESULTED" to 5 ))
        var result = caseWorkflowService.getOutcomeCountsByState(courtCode)
        verify(hearingOutcomeRepositoryCustom).getDynamicOutcomeCountsByState(courtCode)
        assertThat(result).isEqualTo(HearingOutcomeCountByState(2, 0, 5))
    }

    @Test
    fun `given no court codes, when process un resulted cases, then invoke processUnResultedCases on repository without court codes`() {
        var caseWorkflowService = CaseWorkflowService(hearingRepository, courtRepository, hearingOutcomeRepositoryCustom,
            telemetryService, listOf(), LocalTime.now().minusHours(1))

        given(hearingRepository.moveUnResultedCasesToOutcomesWorkflow()).willReturn(Optional.of(2))

        caseWorkflowService.processUnResultedCases()

        verify(hearingRepository).moveUnResultedCasesToOutcomesWorkflow()
        verifyNoMoreInteractions(hearingRepository)
        verify(telemetryService).trackMoveUnResultedCasesToOutcomesFlowJob(2, listOf(), null)
    }
    @Test
    fun `given court codes, when process un resulted cases, then invoke processUnResultedCases on repository with court codes`() {
        val courtCodes = listOf("CRT001", "CRT002")
        var caseWorkflowService = CaseWorkflowService(hearingRepository, courtRepository, hearingOutcomeRepositoryCustom,
            telemetryService, courtCodes, LocalTime.now().minusHours(1))

        given(hearingRepository.moveUnResultedCasesToOutcomesWorkflow(courtCodes)).willReturn(Optional.of(2))

        caseWorkflowService.processUnResultedCases()

        verify(hearingRepository).moveUnResultedCasesToOutcomesWorkflow(courtCodes)
        verifyNoMoreInteractions(hearingRepository)
        verify(telemetryService).trackMoveUnResultedCasesToOutcomesFlowJob(2, courtCodes, null)
    }

    @Test
    fun `given invoked before cut off time, when process un resulted cases, then throw error`() {
        val cutOffTime = LocalTime.now().plusHours(1)
        var caseWorkflowService = CaseWorkflowService(hearingRepository, courtRepository, hearingOutcomeRepositoryCustom,
            telemetryService, listOf(), cutOffTime
        )

        var e = assertThrows(HttpClientErrorException::class.java) { caseWorkflowService.processUnResultedCases() }

        assertThat(e.message).isEqualTo("400 Invoked before cutoff time: $cutOffTime")
        verifyNoInteractions(hearingRepository)
        verify(telemetryService).trackMoveUnResultedCasesToOutcomesFlowJob(0, listOf(), e)
    }
}