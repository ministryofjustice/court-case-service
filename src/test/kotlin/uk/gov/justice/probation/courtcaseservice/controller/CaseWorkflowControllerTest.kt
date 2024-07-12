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
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeCaseList
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeAssignToRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeCountByState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.NEW
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType.ADJOURNED
import java.security.Principal

@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowControllerTest {

    companion object {
        val TEST_COURT_ROOMS = listOf("01", "Court room - 2")
        private val hearingId = "test-hearing-id"
        private val defendantId = "test-defendant-id"
    }

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
        caseWorkflowController.addOrUpdateHearingOutcome(hearingId, defendantId, HearingOutcome(ADJOURNED))
        verify(caseWorkflowService).addOrUpdateHearingOutcome(hearingId, defendantId, ADJOURNED)
    }
    @Test
    fun `should invoke service with court code and hearing state`() {
        val hearingOutcomeSearchRequest = HearingOutcomeSearchRequest(NEW)
        given(caseWorkflowService.fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)).willReturn(
            HearingOutcomeCaseList(listOf(), HearingOutcomeCountByState(1,0,2), TEST_COURT_ROOMS, 2, 2, 9)
        )
        val resp = caseWorkflowController.fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        verify(caseWorkflowService).fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        assertThat(resp).isEqualTo(HearingOutcomeCaseList(listOf(), HearingOutcomeCountByState(1,0,2), TEST_COURT_ROOMS,2, 2, 9))
    }

    @Test
    fun `should invoke service with user details`() {
        // Given
        val hearingOutcome = HearingOutcomeAssignToRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")

        // When
        caseWorkflowController.assignUserToHearingOutcome(hearingId, defendantId, hearingOutcome, principal)

        // Then
        verify(caseWorkflowService).assignAndUpdateStateToInProgress(hearingId, defendantId, "John Smith", "test-uuid")
    }

    @Test
    fun `should invoke service to result the case outcome`() {
        // Given
        val hearingOutcome = HearingOutcomeAssignToRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")
        given(authenticationHelper.getAuthUserName(any(Principal::class.java))).willReturn("test-user-name")
        given(authenticationHelper.getAuthUserId(any(Principal::class.java))).willReturn("test-user-id")
        given(authenticationHelper.getAuthSource(any(Principal::class.java))).willReturn("test-source")

        // When
        caseWorkflowController.resultHearingOutcome(hearingId, defendantId, principal)

        // Then
        verify(caseWorkflowService).resultHearingOutcome(hearingId, defendantId,"test-uuid", "test-user-id", "test-user-name", "test-source")
    }

    @Test
    fun `should return a list of outcome types which is the same size as the enum class`() {
        // Given
        val expectedResult = HearingOutcomeType.entries.toTypedArray()
        // When
        val resp = caseWorkflowController.returnHearingOutcomeTypes()
        // Then
        assertThat(resp.size).isEqualTo(expectedResult.size)
    }

    @Test
    fun `should invoke service to place a hearing on hold`() {
        // Given
        val hearingOutcome = HearingOutcomeAssignToRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")

        // When
        caseWorkflowController.holdHearingOutcome(hearingId, defendantId, principal)

        // Then
        verify(caseWorkflowService).holdHearingOutcome(hearingId, defendantId,"test-uuid")
    }
}