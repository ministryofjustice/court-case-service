package uk.gov.justice.probation.courtcaseservice.controller.model

data class SubjectAccessRequestResponse(
    val hearingOutcomes: List<SARHearingOutcome>,
    val defendantNotes: List<SARHearingDefendantNote>
)