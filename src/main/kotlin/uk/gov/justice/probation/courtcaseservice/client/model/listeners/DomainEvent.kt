package uk.gov.justice.probation.courtcaseservice.client.model.listeners

data class DomainEvent(
  val eventType: String,
  val detailUrl: String,
  val personReference: PersonReference? = null,
)
