package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import java.security.Principal
import javax.validation.Valid


@Tag(name = "Case workflow API")
@RestController
class CaseWorkflowController(val caseWorkflowService: CaseWorkflowService, val authenticationHelper: AuthenticationHelper) {

    @Operation(description = "Adds or updates hearing outcome for a hearing.")
    @PutMapping(value = ["/hearing/{hearingId}/outcome"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun addOrUpdateHearingOutcome(@PathVariable("hearingId") hearingId: String, @RequestBody hearingOutcome: HearingOutcome) =
        caseWorkflowService.addOrUpdateHearingOutcome(hearingId, hearingOutcome.hearingOutcomeType)

    @Operation(description = "Assigns a hearing outcome to the current user")
    @PutMapping(value = ["/hearing/{hearingId}/outcome/assign"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun assignUserToHearingOutcome(@PathVariable("hearingId") hearingId: String, @RequestBody hearingOutcomeAssignToRequest: HearingOutcomeAssignToRequest, principal: Principal) =
            caseWorkflowService.assignAndUpdateStateToInProgress(hearingId, hearingOutcomeAssignToRequest.assignedTo, authenticationHelper.getAuthUserUuid(principal))

    @Operation(description = "Processes a hearing outcome for resulted for the given hearing Id.")
    @PostMapping(value = ["/hearing/{hearingId}/outcome/result"], produces = [APPLICATION_JSON_VALUE])
    fun resultHearingOutcome(@PathVariable("hearingId") hearingId: String, principal: Principal) =
            caseWorkflowService.resultHearingOutcome(hearingId, authenticationHelper.getAuthUserUuid(principal))

    @Operation(description = "Fetch hearing outcomes")
    @GetMapping(value = ["/courts/{courtCode}/hearing-outcomes"], produces = [APPLICATION_JSON_VALUE])
    fun fetchHearingOutcomes(@PathVariable("courtCode") courtCode: String, @Valid hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): HearingOutcomeCaseList {
        val cases = caseWorkflowService.getOutcomeCountsByState(courtCode, hearingOutcomeSearchRequest)
        return HearingOutcomeCaseList(cases, caseWorkflowService.getOutcomeCountsByState(courtCode))
    }
}