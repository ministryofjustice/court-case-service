package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeCaseList
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType.ADJOURNED

@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowControllerTest {

    @Mock
    lateinit var caseWorkflowService: CaseWorkflowService

    @InjectMocks
    lateinit var caseWorkflowController: CaseWorkflowController

    @Test
    fun `should invoke service with hearing id and outcome type`() {
        val hearingId = "test-hearing-id"
        caseWorkflowController.addHearingOutcome(hearingId, HearingOutcome(ADJOURNED))
        verify(caseWorkflowService).addHearingOutcome(hearingId, ADJOURNED)
    }
    @Test
    fun `should invoke service with court code and hearing state`() {
        given(caseWorkflowService.fetchHearingOutcomes(COURT_CODE, HearingOutcomeItemState.NEW)).willReturn(listOf())
        val resp = caseWorkflowController.fetchHearingOutcomes(COURT_CODE, HearingOutcomeItemState.NEW)
        verify(caseWorkflowService).fetchHearingOutcomes(COURT_CODE, HearingOutcomeItemState.NEW)
        assertThat(resp).isEqualTo(HearingOutcomeCaseList(listOf()))
    }
}