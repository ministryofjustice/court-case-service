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

    @Operation(description = "Adds hearing outcome for a hearing.")
    @PutMapping(value = ["/hearing/{hearingId}/outcome"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun addHearingOutcome(@PathVariable("hearingId") hearingId: String, @RequestBody hearingOutcome: HearingOutcome) =
        caseWorkflowService.addHearingOutcome(hearingId, hearingOutcome.hearingOutcomeType)

    @Operation(description = "Assigns a hearing outcome to the current user")
    @PutMapping(value = ["/hearing/{hearingId}/outcome/assign"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun assignUserToHearingOutcome(@PathVariable("hearingId") hearingId: String, @RequestBody hearingOutcome: HearingOutcomeAssignRequest, principal: Principal) =
            caseWorkflowService.assignHearingOutcomeTo(hearingId, hearingOutcome.assignedTo, authenticationHelper.getAuthUserUuid(principal))

    @Operation(description = "Fetch hearing outcomes")
    @GetMapping(value = ["/courts/{courtCode}/hearing-outcomes"], params = ["state"], produces = [APPLICATION_JSON_VALUE])
    fun fetchHearingOutcomes(@PathVariable("courtCode") courtCode: String, @Valid hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): HearingOutcomeCaseList {
        return HearingOutcomeCaseList(caseWorkflowService.fetchHearingOutcomes(courtCode, hearingOutcomeSearchRequest))
    }
}