package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class HearingNotesSarResponse(
  val note: String = "",
  val authorSurname: String = "",
  val created: LocalDateTime? = null,
  val createdBy: String = "",
  val lastUpdated: LocalDateTime? = null,
  val lastUpdatedBy: String = "",
)
