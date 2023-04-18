package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService


@Tag(name = "Case workflow API")
@RestController
class CaseWorkflowController(val caseWorkflowService: CaseWorkflowService) {

    @Operation(description = "Adds hearing outcome for a hearing.")
    @PutMapping(value = ["/hearing/{hearingId}/outcome"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun addHearingOutcome(@PathVariable("hearingId") hearingId: String, @RequestBody hearingOutcome: HearingOutcome) =
        caseWorkflowService.addHearingOutcome(hearingId, hearingOutcome.hearingOutcomeType)
}