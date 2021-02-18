package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProbationStatus {
    CURRENT("Current"),
    PREVIOUSLY_KNOWN("Previously known"),
    NOT_SENTENCED("Not sentenced"),
    NO_RECORD("No record");

    private final String name;

    ProbationStatus(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}

