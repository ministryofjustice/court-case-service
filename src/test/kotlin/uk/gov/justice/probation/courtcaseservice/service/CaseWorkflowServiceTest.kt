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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.*
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
        val hearingId = "hearing-id-one"
        val defendantId = "defendant-id-one"
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
    fun `given hearing outcome and hearing id and defendant id exist should add hearing outcome`() {
        val dbHearingEntity = aHearingEntity()
        given(hearingRepository.findFirstByHearingId(HEARING_ID)).willReturn(Optional.of(dbHearingEntity))
        caseWorkflowService.addOrUpdateHearingOutcome(HEARING_ID, DEFENDANT_ID, HearingOutcomeType.REPORT_REQUESTED)
        verify(hearingRepository).findFirstByHearingId(hearingId)
        assertThat(dbHearingEntity.hearingDefendants[0].hearingOutcome)
            .isEqualTo(HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").build())
    }

    @Test
    fun `given hearing outcome and hearing outcome exists, should update hearing outcome`() {
        val hearingOutcome = HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.ADJOURNED.name).build()
        val dbHearingEntity: HearingEntity = aHearingEntity()
            .withHearingDefendants(listOf(HearingDefendantEntity.builder().defendantId(DEFENDANT_ID).hearingOutcome(hearingOutcome).build()))
        given(hearingRepository.findFirstByHearingId(HEARING_ID)).willReturn(Optional.of(dbHearingEntity))
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(dbHearingEntity))
        caseWorkflowService.addOrUpdateHearingOutcome(hearingId, defendantId, HearingOutcomeType.REPORT_REQUESTED)
        verify(hearingRepository).findFirstByHearingId(hearingId)
        assertThat(dbHearingEntity.hearingDefendants[0].hearingOutcome)
            .isEqualTo(HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").build())
    }

    @Test
    fun `given hearing outcome and hearing id does not exist should throw entity not found exception`() {
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.empty())
        assertThrows(
            "Hearing not found with id hearing-id-one",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.addOrUpdateHearingOutcome(
                hearingId,
                defendantId,
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
        val hearingOutcome = HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.ADJOURNED.name).build()
        val hearingEntity: HearingEntity = aHearingEntity()
            .withHearingDefendants(listOf(HearingDefendantEntity.builder().defendantId(DEFENDANT_ID).hearingOutcome(hearingOutcome).build()))

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        caseWorkflowService.assignAndUpdateStateToInProgress(hearingId, defendantId, assignedTo, assignedToUuid)

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository).save(hearingEntityCaptor.capture())

        val expectedHearingOutcome = hearingEntity.hearingDefendants[0].hearingOutcome
        assertThat(expectedHearingOutcome.state).isEqualTo(HearingOutcomeItemState.IN_PROGRESS.name)
        assertThat(expectedHearingOutcome.assignedTo).isEqualTo(assignedTo)
        assertThat(expectedHearingOutcome.assignedToUuid).isEqualTo(assignedToUuid)
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
                    defendantId,
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

        val hearing1 = aHearingEntityWithHearingId("case-id-1", "hearing-id-1", "defendant-id-1")
            .withHearingDefendants(listOf( HearingDefendantEntity.builder().defendantId("defendant-id-1").hearingOutcome(hearingOutcomeEntity1).build()))
        val hearing2 = aHearingEntityWithHearingId("case-id-2", "hearing-id-2", "defendant-id-2")
            .withHearingDefendants(listOf( HearingDefendantEntity.builder().defendantId("defendant-id-2").hearingOutcome(hearingOutcomeEntity2).build()))

        given(courtRepository.findByCourtCode(COURT_CODE)).willReturn(Optional.of(CourtEntity.builder().build()))
        given(
            hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(
                COURT_CODE,
                HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW)
            )
        ).willReturn(
            PageImpl(
                listOf(
                    Pair<HearingEntity, LocalDate>(hearing1, SESSION_START_TIME.toLocalDate()),
                    Pair<HearingEntity, LocalDate>(hearing2, SESSION_START_TIME.toLocalDate())
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
                        hearingDate = SESSION_START_TIME.toLocalDate(),
                        hearingId = "hearing-id-1",
                        defendantId = "defendant-id-1",
                        probationStatus = PROBATION_STATUS,
                        offences = listOf(OFFENCE_TITLE),
                        defendantName = DEFENDANT_NAME,
                        crn = "X340906",
                        state = HearingOutcomeItemState.NEW
                    ),
                    HearingOutcomeResponse(
                        hearingOutcomeType = HearingOutcomeType.ADJOURNED,
                        outcomeDate = LocalDateTime.of(2023, 5, 5, 19, 9, 5),
                        hearingDate = SESSION_START_TIME.toLocalDate(),
                        hearingId = "hearing-id-2",
                        defendantId = "defendant-id-2",
                        probationStatus = PROBATION_STATUS,
                        offences = listOf(OFFENCE_TITLE),
                        defendantName = DEFENDANT_NAME,
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
        val assignedToUuid = "test-uuid"
        val hearingEntity = HearingEntity.builder().hearingDefendants(listOf(HearingDefendantEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build()).build()))
            .build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        caseWorkflowService.resultHearingOutcome(hearingId, defendantId, assignedToUuid)

        // Then
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verify(hearingRepository).save(hearingEntityCaptor.capture())

        val actual = hearingEntityCaptor.value.hearingDefendants[0].hearingOutcome
        assertThat(actual.state).isEqualTo(HearingOutcomeItemState.RESULTED.name)
        assertThat(actual.resultedDate).isNotNull()
    }

    @Test
    fun `given existing hearing with outcome in IN_PROGRESS and NOT allocated to current user, should throw forbidden error`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"

        val hearingEntity = HearingEntity.builder().hearingDefendants(listOf(HearingDefendantEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.IN_PROGRESS.name).assignedToUuid(assignedToUuid).build())
            .build())).build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Outcome not allocated to current user.",
            ForbiddenException::class.java
        ) {
            caseWorkflowService.resultHearingOutcome(hearingId, defendantId,"un-allocated-to-user")
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
        verifyNoMoreInteractions(hearingRepository)
    }

    @Test
    fun `given existing hearing with outcome NOT in IN_PROGRESS state, should throw bad request error`() {
        // Given
        val hearingId = "hearing-id-one"
        val assignedToUuid = "test-uuid"

        val hearingEntity = HearingEntity.builder().hearingDefendants(listOf(HearingDefendantEntity.builder()
            .hearingOutcome(HearingOutcomeEntity.builder().state(HearingOutcomeItemState.NEW.name).assignedToUuid(assignedToUuid).build())
            .build())).build()

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(hearingEntity))

        // When
        assertThrows(
            "Invalid state for outcome to be resulted.",
            ResponseStatusException::class.java
        ) {
            caseWorkflowService.resultHearingOutcome(hearingId, defendantId, assignedToUuid)
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

    @Test
    fun `given hearing id and defendant id and defendant id does not exist, when result hearing outcome, should throw entity not found exception`() {

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(aHearingEntity()))
        assertThrows(
            "Defendant invalid-defendant-id not found on hearing with id $HEARING_ID",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.resultHearingOutcome(
                HEARING_ID,
                "invalid-defendant-id",
                "test-user-uuid"
            )
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
    }

    @Test
    fun `given hearing id and defendant id and defendant id does not exist, when assing hearing outcome, should throw entity not found exception`() {

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(aHearingEntity()))
        assertThrows(
            "Defendant invalid-defendant-id not found on hearing with id $HEARING_ID",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.assignAndUpdateStateToInProgress(
                HEARING_ID,
                "invalid-defendant-id",
                "User Two",
                "test-user-uuid"
            )
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
    }

    @Test
    fun `given hearing id and defendant id and defendant id does not exist, when add or update outcome, should throw entity not found exception`() {

        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(aHearingEntity()))
        assertThrows(
            "Defendant invalid-defendant-id not found on hearing with id $HEARING_ID",
            EntityNotFoundException::class.java
        ) {
            caseWorkflowService.addOrUpdateHearingOutcome(
                HEARING_ID,
                "invalid-defendant-id",
                HearingOutcomeType.REPORT_REQUESTED
            )
        }
        verify(hearingRepository).findFirstByHearingId(hearingId)
    }

}