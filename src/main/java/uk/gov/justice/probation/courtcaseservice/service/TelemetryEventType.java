package uk.gov.justice.probation.courtcaseservice.service;

enum TelemetryEventType {
    COURT_CASE_UPDATED("PiCCourtCaseUpdated"),
    COURT_CASE_CREATED("PiCCourtCaseCreated"),
    COURT_CASE_DELETED("PiCCourtCaseDeleted");

    final String eventName;

    TelemetryEventType(String eventName) {
        this.eventName = eventName;
    }
}
