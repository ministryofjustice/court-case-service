package uk.gov.justice.probation.courtcaseservice.controller.model

data class OffenceSarResponse(
  val title: String,
  val summary: String,
  val act: String,
  val sequence: Int?,
  val listNo: Int?,
  val offenceCode: String?,
  val plea: PleaSarResponse?,
  val verdict: VerdictSarResponse?,
  val judicialResults: List<JudicialResultSarResponse>?,
)
