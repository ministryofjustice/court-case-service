package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Hearing outcome counts by workflow state.")
data class HearingOutcomeCountByState(
  val toResultCount: Int,
  val inProgressCount: Int,
  val resultedCount: Int,
)
