package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeAssignToRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeCaseList
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.security.Principal

@Tag(name = "Case workflow API")
@RestController
class CaseWorkflowController(val caseWorkflowService: CaseWorkflowService, val authenticationHelper: AuthenticationHelper) {

  @Operation(description = "Adds or updates hearing outcome for a hearing.")
  @PutMapping(value = ["/hearing/{hearingId}/defendant/{defendantId}/outcome"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
  fun addOrUpdateHearingOutcome(@PathVariable("hearingId") hearingId: String, @PathVariable("defendantId") defendantId: String, @RequestBody hearingOutcome: HearingOutcome) = caseWorkflowService.addOrUpdateHearingOutcome(hearingId, defendantId, hearingOutcome.hearingOutcomeType)

  @Operation(description = "Assigns a hearing outcome to the current user")
  @PutMapping(value = ["/hearing/{hearingId}/defendant/{defendantId}/outcome/assign"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
  fun assignUserToHearingOutcome(@PathVariable("hearingId") hearingId: String, @PathVariable("defendantId") defendantId: String, @RequestBody hearingOutcomeAssignToRequest: HearingOutcomeAssignToRequest, principal: Principal) = caseWorkflowService.assignAndUpdateStateToInProgress(hearingId, defendantId, hearingOutcomeAssignToRequest.assignedTo, authenticationHelper.getAuthUserUuid(principal))

  @Operation(description = "Processes a hearing outcome for resulted for the given hearing Id.")
  @PostMapping(value = ["/hearing/{hearingId}/defendant/{defendantId}/outcome/result"], produces = [APPLICATION_JSON_VALUE])
  fun resultHearingOutcome(@PathVariable("hearingId") hearingId: String, @PathVariable("defendantId") defendantId: String, principal: Principal) = caseWorkflowService.resultHearingOutcome(hearingId, defendantId, authenticationHelper.getAuthUserUuid(principal), authenticationHelper.getAuthUserId(principal), authenticationHelper.getAuthUserName(principal), authenticationHelper.getAuthSource(principal))

  @Operation(description = "Fetch hearing outcomes")
  @GetMapping(value = ["/courts/{courtCode}/hearing-outcomes"], produces = [APPLICATION_JSON_VALUE])
  fun fetchHearingOutcomes(@PathVariable("courtCode") courtCode: String, @Valid @ParameterObject hearingOutcomeSearchRequest: HearingOutcomeSearchRequest): HearingOutcomeCaseList = caseWorkflowService.fetchHearingOutcomes(courtCode, hearingOutcomeSearchRequest)

  @Operation(description = "Triggers move un resulted cases to outcomes workflow")
  @Hidden
  @PutMapping(value = ["/process-un-resulted-cases"], produces = [APPLICATION_JSON_VALUE])
  fun processUnResultedCases() = caseWorkflowService.processUnResultedCases()

  @Operation(description = "Return Hearing Outcome Types")
  @GetMapping(value = ["/hearing-outcome-types"], produces = [APPLICATION_JSON_VALUE])
  @Cacheable("hearing-outcome-types")
  fun returnHearingOutcomeTypes(): List<Map<String, String>> = HearingOutcomeType.entries.map { outcome ->
    mapOf("value" to outcome.name, "label" to outcome.value)
  }

  @Operation(description = "Updates prep status for for a given hearing and defendant")
  @PutMapping(value = ["/hearing/{hearingId}/defendants/{defendantId}/prep-status/{prepStatus}"])
  fun updatePrepStatus(
    @PathVariable("hearingId") hearingId: String,
    @PathVariable("defendantId") defendantId: String,
    @PathVariable("prepStatus") prepStatus: HearingPrepStatus,
  ) = caseWorkflowService.updatePrepStatus(hearingId, defendantId, prepStatus)
}
