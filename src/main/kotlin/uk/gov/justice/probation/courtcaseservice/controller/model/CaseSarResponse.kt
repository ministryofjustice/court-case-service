package uk.gov.justice.probation.courtcaseservice.controller.model

data class CaseSarResponse(
    val urn: String,
    val hearings: MutableList<HearingSarResponse> = mutableListOf(),
    val comments: List<CaseCommentsSarResponse>
)