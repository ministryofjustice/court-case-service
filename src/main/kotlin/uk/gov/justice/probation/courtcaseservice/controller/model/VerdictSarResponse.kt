package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDate

data class VerdictSarResponse(
  var verdictType: String?,
  var date: LocalDate?,
)
