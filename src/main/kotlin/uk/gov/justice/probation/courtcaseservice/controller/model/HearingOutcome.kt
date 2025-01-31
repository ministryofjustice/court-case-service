package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

@Schema(description = "Hearing outcome input model")
data class HearingOutcome(@JsonProperty("hearingOutcomeType") val hearingOutcomeType: HearingOutcomeType)
