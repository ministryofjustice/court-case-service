package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LibraEnrichmentTargets(
  @get:JsonProperty("cIds")
  val cIds: List<String>,
)
