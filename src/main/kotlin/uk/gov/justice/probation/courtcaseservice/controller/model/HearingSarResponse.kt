package uk.gov.justice.probation.courtcaseservice.controller.model

data class HearingSarResponse(
  val hearingId: String,
  val hearingEventType: String,
  val notes: List<HearingNotesSarResponse> = emptyList(),
  val outcomes: List<HearingOutcomeSarResponse> = emptyList(),
  val defendant: DefendantSarResponse,
)
