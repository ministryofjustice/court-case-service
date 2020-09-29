package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProbationStatus {
    CURRENT("Current"),
    PREVIOUSLY_KNOWN("Previously known");

    private final String name;

    ProbationStatus(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}

