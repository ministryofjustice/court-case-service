package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class CaseCommentsSarResponse(
  val comment: String = "",
  val authorSurname: String = "",
  val created: LocalDateTime? = null,
  val lastUpdated: LocalDateTime? = null,
  val lastUpdatedBy: String = "",
  val caseNumber: String = "",
  val createdBy: String = "",
  val caseId: String = "",
)
