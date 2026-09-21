package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDate

data class SeedScenarioRequest(
  val count: Int? = null,
  val start: LocalDate? = null,
  val days: Int? = null,
  val court: String? = null,
  val clean: Boolean? = null,
  val caseMarkers: List<String> = emptyList(),
  val defendantName: String? = null,
  val withOffender: Boolean = false,
  val withOutcomes: Boolean = false,
)
