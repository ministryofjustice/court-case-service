package uk.gov.justice.probation.courtcaseservice.controller.model

data class JudicialResultSarResponse(
  var label: String?,
  var isConvictedResult: Boolean?,
  var judicialResultTypeId: String?,
  var resultText: String?,
)
