package uk.gov.justice.probation.courtcaseservice.service;

enum TelemetryEventType {
    COURT_CASE_UPDATED("PiCCourtCaseUpdated"),
    COURT_CASE_CREATED("PiCCourtCaseCreated"),
    COURT_CASE_DELETED("PiCCourtCaseDeleted"),
    MATCH_CONFIRMED("PiCMatchConfirmed"),
    MATCH_REJECTED("PiCMatchRejected"),
    DEFENDANT_LINKED("PiCDefendantLinked"),
    DEFENDANT_UNLINKED("PiCDefendantUnlinked")
    ;

    final String eventName;

    TelemetryEventType(String eventName) {
        this.eventName = eventName;
    }
}
