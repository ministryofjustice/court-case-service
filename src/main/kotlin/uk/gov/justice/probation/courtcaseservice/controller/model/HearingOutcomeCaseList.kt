package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Hearing outcome response model")
data class HearingOutcomeCaseList(
    val cases: List<HearingOutcomeResponse>,
    val countsByState: HearingOutcomeCountByState
)