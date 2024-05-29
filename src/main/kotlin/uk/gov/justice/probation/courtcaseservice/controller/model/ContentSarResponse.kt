package uk.gov.justice.probation.courtcaseservice.controller.model

data class ContentSarResponse(val comments: List<CaseCommentsSarResponse>, val hearingOutcomes: List<HearingOutcomeSarResponse>, val hearingNotes: List<HearingNotesSarResponse>)
