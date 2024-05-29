package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class HearingOutcomeSarResponse(
    val outcomeType: String,
    val outcomeDate: LocalDateTime?,
    val resultedDate: LocalDateTime?,
    val state: String,
    val assignedTo: String?,
    val createdDate: LocalDateTime
)
