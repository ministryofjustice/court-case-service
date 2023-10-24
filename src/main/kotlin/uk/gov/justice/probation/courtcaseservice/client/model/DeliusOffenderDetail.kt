package uk.gov.justice.probation.courtcaseservice.client.model

import java.time.LocalDate

data class DeliusOffenderDetail(
    val name: Name,
    val identifiers: Identifiers,
    val dateOfBirth: LocalDate? = null

)
