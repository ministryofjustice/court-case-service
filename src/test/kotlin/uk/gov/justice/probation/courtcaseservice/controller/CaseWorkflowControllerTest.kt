package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.NEW
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingDefendantOutcomesRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeAssignedUser
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType.ADJOURNED
import java.security.Principal
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCaseList as V2HearingOutcomeCaseList
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCountByState as V2HearingOutcomeCountByState


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
            HearingOutcomeCaseList(listOf(), HearingOutcomeCountByState(1,0,2), TEST_COURT_ROOMS,1,1,1, listOf<HearingOutcomeAssignedUser>())
        )
        val resp = caseWorkflowController.fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        verify(caseWorkflowService).fetchHearingOutcomes(COURT_CODE, hearingOutcomeSearchRequest)
        assertThat(resp).isEqualTo(HearingOutcomeCaseList(listOf(), HearingOutcomeCountByState(1,0,2), TEST_COURT_ROOMS,1, 1, 1, listOf<HearingOutcomeAssignedUser>()))
    }

    @Test
    fun `v2 hearing defendant outcomes should invoke service with court code and hearing state`() {
        val hearingDefendantOutcomes = HearingDefendantOutcomesRequest(state = NEW)
        given(caseWorkflowService.fetchV2HearingDefendantOutcomes(COURT_CODE, hearingDefendantOutcomes)).willReturn(
            V2HearingOutcomeCaseList(
                listOf(),
                V2HearingOutcomeCountByState(listOf(Pair("NEW", 1), Pair("IN_PROGRESS",0), Pair("Resulted", 2))),
                TEST_COURT_ROOMS,
                1,
                1,
                1,
                listOf<HearingOutcomeAssignedUser>(HearingOutcomeAssignedUser("John Doe", "UUID"))
            )
        )
        val resp = caseWorkflowController.fetchHearingDefendantOutcomesV2(COURT_CODE, hearingDefendantOutcomes)
        verify(caseWorkflowService).fetchV2HearingDefendantOutcomes(COURT_CODE, hearingDefendantOutcomes)

        assertThat(resp.body.records).isEqualTo(listOf<HearingOutcomeResponse>())
        assertThat(resp.body.filters).hasSize(3)
        assertThat(resp.body.filters[0].id).isEqualTo("assignedUsers")
        assertThat(resp.body.filters[0].name).isEqualTo("Assigned Users")
        assertThat(resp.body.filters[0].items).hasSize(1)
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

        // When
        caseWorkflowController.resultHearingOutcome(hearingId, defendantId, principal)

        // Then
        verify(caseWorkflowService).resultHearingOutcome(hearingId, defendantId,"test-uuid")
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
}