package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Hearing outcome assigned to input model")
data class HearingOutcomeAssignToRequest(
    val assignedTo: String
)
