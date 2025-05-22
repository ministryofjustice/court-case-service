package uk.gov.justice.probation.courtcaseservice.controller.model

data class HearingSarResponse(
  val hearingId: String,
  val notes: List<HearingNotesSarResponse>,
  val outcomes: List<HearingOutcomeSarResponse> = emptyList(),
)
