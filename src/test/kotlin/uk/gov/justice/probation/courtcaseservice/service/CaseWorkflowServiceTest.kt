package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowServiceTest {

    @Mock
    lateinit var hearingRepository: HearingRepository

    @Mock
    lateinit var hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom

    @Mock
    lateinit var courtRepository: CourtRepository

    @InjectMocks
    lateinit var caseWorkflowService: CaseWorkflowService

    @Captor
    lateinit var hearingEntityCaptor: ArgumentCaptor<HearingEntity>

    @Test
    fun `given hearing outcome and hearing id exist should add hearing outcome`() {
        val hearingId = "hearing-id-one"
        val dbHearingEntity = HearingEntity.builder().build()
        given(hearingRepository.findFirstByHearingId(hearingId)).willReturn(Optional.of(dbHearingEntity))
        caseWorkflowService.addHearingOutcome(hearingId, HearingOutcomeType.REPORT_REQUESTED)
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
            caseWorkflowService.addHearingOutcome(
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
        caseWorkflowService.assignHearingOutcomeTo(hearingId, assignedTo, assignedToUuid)

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

            caseWorkflowService.assignHearingOutcomeTo(
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
            LocalDateTime.of(2023, 6,6, 19, 9, 1)).build()
        val hearingOutcomeEntity2 = HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.ADJOURNED.name).outcomeDate(
            LocalDateTime.of(2023, 5,5, 19, 9, 5)).build()

        val hearing1 = EntityHelper.aHearingEntityWithHearingId("case-id-1", "hearing-id-1", "defendant-id-1").withHearingOutcome(hearingOutcomeEntity1)
        val hearing2 = EntityHelper.aHearingEntityWithHearingId("case-id-2", "hearing-id-2", "defendant-id-2").withHearingOutcome(hearingOutcomeEntity2)

        given(courtRepository.findByCourtCode(COURT_CODE)).willReturn(Optional.of(CourtEntity.builder().build()))
        given(hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome(COURT_CODE, HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW))).willReturn(
            listOf(hearing1, hearing2)
        )

        val hearingOutcomes = caseWorkflowService.fetchHearingOutcomes(COURT_CODE, HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW))

        assertThat(hearingOutcomes).isEqualTo(listOf(
            HearingOutcomeResponse(
                hearingOutcomeType = HearingOutcomeType.REPORT_REQUESTED,
                outcomeDate = LocalDateTime.of(2023, 6,6, 19, 9, 1),
                hearingDate = EntityHelper.SESSION_START_TIME.toLocalDate(),
                hearingId = "hearing-id-1",
                defendantId = "defendant-id-1",
                probationStatus = EntityHelper.PROBATION_STATUS,
                offences = listOf(EntityHelper.OFFENCE_TITLE),
                defendantName = EntityHelper.DEFENDANT_NAME
            ),
            HearingOutcomeResponse(
                hearingOutcomeType = HearingOutcomeType.ADJOURNED,
                outcomeDate = LocalDateTime.of(2023, 5,5, 19, 9, 5),
                hearingDate = EntityHelper.SESSION_START_TIME.toLocalDate(),
                hearingId = "hearing-id-2",
                defendantId = "defendant-id-2",
                probationStatus = EntityHelper.PROBATION_STATUS,
                offences = listOf(EntityHelper.OFFENCE_TITLE),
                defendantName = EntityHelper.DEFENDANT_NAME
            )
        ))
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
}