package uk.gov.justice.probation.courtcaseservice.controller.model

data class CaseSarResponse(
    val caseUrn: String,
    val hearings: MutableList<HearingSarResponse>,
    val caseComments: List<CaseCommentsSarResponse>
)