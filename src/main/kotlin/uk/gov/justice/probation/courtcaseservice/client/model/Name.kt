package uk.gov.justice.probation.courtcaseservice.client.model

data class Name(
    val foreName: String? = null,
    val surName: String? = null,
    val otherNames: List<String>? = emptyList()
)
