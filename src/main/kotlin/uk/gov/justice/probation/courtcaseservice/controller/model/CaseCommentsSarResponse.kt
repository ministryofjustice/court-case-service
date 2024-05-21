package uk.gov.justice.probation.courtcaseservice.controller.model

import java.time.LocalDateTime

data class CaseCommentsSarResponse(val comment: String,
                                   val author: String,
                                   val created: LocalDateTime,
                                   val createdBy: String,
                                   val lastUpdated: LocalDateTime,
                                   val lastUpdatedBy: String,
                                   val caseNumber: String)
