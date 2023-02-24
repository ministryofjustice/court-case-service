package uk.gov.justice.probation.courtcaseservice.service;

public enum TelemetryEventType {
    COURT_CASE_UPDATED("PiCCourtCaseUpdated"),
    COURT_CASE_CREATED("PiCCourtCaseCreated"),
    COURT_CASE_DELETED("PiCCourtCaseDeleted"),
    MATCH_CONFIRMED("PiCMatchConfirmed"),
    MATCH_REJECTED("PiCMatchRejected"),
    DEFENDANT_LINKED("PiCDefendantLinked"),
    DEFENDANT_UNLINKED("PiCDefendantUnlinked"),
    GRACEFUL_DEGRADE("PiCGracefulDegrade"),
    CASE_COMMENT_ADDED("PicCourtCaseCommentAdded"),
    CASE_COMMENT_DELETED("PicCourtCaseCommentDeleted"),
    HEARING_NOTE_ADDED("PicHearingNoteCreated"),
    HEARING_NOTE_UPDATED("PicHearingNoteUpdated"),
    HEARING_NOTE_DELETED("PicHearingNoteDeleted"),
    OFFENDER_PROBATION_STATUS_UPDATED("PiCOffenderProbationStatusUpdated"),
    OFFENDER_PROBATION_STATUS_NOT_UPDATED("PiCOffenderProbationStatusNotUpdated")
    ;

    final String eventName;

    TelemetryEventType(String eventName) {
        this.eventName = eventName;
    }
}
