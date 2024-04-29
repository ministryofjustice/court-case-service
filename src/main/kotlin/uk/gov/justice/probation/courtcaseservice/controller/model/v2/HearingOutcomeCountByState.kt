package uk.gov.justice.probation.courtcaseservice.controller.model.v2

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Hearing outcome counts by workflow state.")
data class HearingOutcomeCountByState(
    val counts: List<Pair<String, Int>>
)