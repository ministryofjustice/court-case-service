package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
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
}