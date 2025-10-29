package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDate

data class PleaSarResponse(
  var pleaValue: String?,
  var date: LocalDate?,
)
