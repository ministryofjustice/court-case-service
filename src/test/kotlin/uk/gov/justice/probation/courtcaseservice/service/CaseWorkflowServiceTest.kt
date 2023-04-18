package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowServiceTest {

    @Mock
    lateinit var hearingRepository: HearingRepository

    @InjectMocks
    lateinit var caseWorkflowService: CaseWorkflowService

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
    fun `given hearing outcome and hearing id does not exist should add hearing outcome`() {
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
}