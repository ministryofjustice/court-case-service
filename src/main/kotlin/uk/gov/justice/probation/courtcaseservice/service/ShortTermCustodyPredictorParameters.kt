package uk.gov.justice.probation.courtcaseservice.service

data class ShortTermCustodyPredictorParameters(
  var offenderAge: Int? = null,
  var offenceCode: String,
  var courtCode: String,
)
