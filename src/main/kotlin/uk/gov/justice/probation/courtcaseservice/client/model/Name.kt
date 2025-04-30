package uk.gov.justice.probation.courtcaseservice.client.model

data class Name(
  val forename: String? = null,
  val surname: String? = null,
  val otherNames: List<String>? = emptyList(),
)
