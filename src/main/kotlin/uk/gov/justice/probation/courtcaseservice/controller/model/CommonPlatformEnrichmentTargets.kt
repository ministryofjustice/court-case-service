package uk.gov.justice.probation.courtcaseservice.controller.model

data class CommonPlatformEnrichmentTargets(
  val defendantIds: List<String>,
  val page: Int,
  val size: Int,
  val totalElements: Long,
  val totalPages: Int,
  val hasNext: Boolean,
)
