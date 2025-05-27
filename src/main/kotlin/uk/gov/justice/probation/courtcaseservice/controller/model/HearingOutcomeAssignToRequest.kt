package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Hearing outcome assigned to input model")
data class HearingOutcomeAssignToRequest(
  @JsonProperty("assignedTo")
  val assignedTo: String,
)
