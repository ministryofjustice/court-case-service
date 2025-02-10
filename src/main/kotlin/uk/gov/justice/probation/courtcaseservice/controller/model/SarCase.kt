package uk.gov.justice.probation.courtcaseservice.controller.model

data class SarCase (
    var caseNo: String,
    val hearingNotesSarResponse: List<HearingNotesSarResponse>,
    val hearingOutcomeSarResponse: List<HearingOutcomeSarResponse>,
    val defendantCaseCommentsService: List<CaseCommentsSarResponse>
)
