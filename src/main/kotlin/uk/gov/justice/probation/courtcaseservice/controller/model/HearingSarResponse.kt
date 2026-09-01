package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class HearingSarResponse(
  val hearingId: String,
  val courtName: String? = null,
  val hearingDateTime: LocalDateTime? = null,
  val notes: List<HearingNotesSarResponse> = emptyList(),
  val outcomes: List<HearingOutcomeSarResponse> = emptyList(),
)
