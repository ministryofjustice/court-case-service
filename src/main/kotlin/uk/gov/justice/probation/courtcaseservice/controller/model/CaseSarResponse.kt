package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class CaseSarResponse(
  val caseId: String,
  val caseNo: String = "",
  val created: LocalDateTime? = null,
  val lastUpdated: LocalDateTime? = null,
  val createdBy: String = "",
  val lastUpdatedBy: String = "",
  val hearings: MutableList<HearingSarResponse> = mutableListOf(),
  val comments: List<CaseCommentsSarResponse>,
)
