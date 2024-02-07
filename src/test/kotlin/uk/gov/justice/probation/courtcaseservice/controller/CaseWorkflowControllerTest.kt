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
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType.ADJOURNED
import java.security.Principal

@ExtendWith(MockitoExtension::class)
internal class CaseWorkflowControllerTest {

    companion object {
        val TEST_COURT_ROOMS = listOf("01", "Court room - 2")
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
        val hearingId = "test-hearing-id"
        caseWorkflowController.addOrUpdateHearingOutcome(hearingId, HearingOutcome(ADJOURNED))
        verify(caseWorkflowService).addOrUpdateHearingOutcome(hearingId, ADJOURNED)
    }
    @Test
    fun `should invoke service with court code and hearing state`() {
        val hearingOutcomeSearchRequest = HearingOutcomeSearchRequest(HearingOutcomeItemState.NEW)
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
        val hearingId = "test-hearing-id"
        val hearingOutcome = HearingOutcomeAssignToRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")

        // When
        caseWorkflowController.assignUserToHearingOutcome(hearingId, hearingOutcome, principal)

        // Then
        verify(caseWorkflowService).assignAndUpdateStateToInProgress(hearingId, "John Smith", "test-uuid")
    }

    @Test
    fun `should invoke service to result the case outcome`() {
        // Given
        val hearingId = "test-hearing-id"
        val hearingOutcome = HearingOutcomeAssignToRequest("John Smith")
        given(authenticationHelper.getAuthUserUuid(any(Principal::class.java))).willReturn("test-uuid")

        // When
        caseWorkflowController.resultHearingOutcome(hearingId, principal)

        // Then
        verify(caseWorkflowService).resultHearingOutcome(hearingId, "test-uuid")
    }

    @Test
    fun `should return a list of outcome types`() {
        // Given
        val expectedResult = HearingOutcomeType.entries.toTypedArray()
        // When
        val resp = caseWorkflowController.returnTypes()
        // Then
        assertThat(resp).isEqualTo(expectedResult)
    }
}