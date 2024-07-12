package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class SARHearingOutcome(
    val hearingId: Long,
    val outcomeType: String,
    val outcomeDate: LocalDateTime?,
    val resultedDate: LocalDateTime?,
    val state: String,
    val assignedTo: String?,
    val createdDate: LocalDateTime
)
