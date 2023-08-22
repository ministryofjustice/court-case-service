package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType.ADJOURNED
import java.security.Principal

@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowControllerTest {

    @Mock
    lateinit var caseWorkflowService: CaseWorkflowService

    @Mock
    lateinit var principal: AuthAwareAuthenticationToken

    @Mock
    lateinit var authenticationHelper: AuthenticationHelper

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
        val hearingOutcomeSearchRequest = HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW)
        given(caseWorkflowService.fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)).willReturn(listOf())
        val resp = caseWorkflowController.fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        verify(caseWorkflowService).fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        assertThat(resp).isEqualTo(HearingOutcomeCaseList(listOf()))
    }

    @Test
    fun `should invoke service with user details`() {
        // Given
        val hearingId = "test-hearing-id"
        val hearingOutcome = HearingOutcomeAssignRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")

        // When
        caseWorkflowController.assignUserToHearingOutcome(hearingId, hearingOutcome, principal)

        // Then
        verify(caseWorkflowService).assignHearingOutcomeTo(hearingId, "John Smith", "test-uuid")
    }
}