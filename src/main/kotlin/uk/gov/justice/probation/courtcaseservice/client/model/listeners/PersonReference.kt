package uk.gov.justice.probation.courtcaseservice.client.model.listeners

data class PersonReference(
    val identifiers: List<PersonIdentifier>? = emptyList()
)
