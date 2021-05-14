package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProbationStatus {
    CURRENT("Current"),
    PREVIOUSLY_KNOWN("Previously known"),
    NOT_SENTENCED("Pre-sentence record"),
    NO_RECORD("No record");

    private static final ProbationStatus DEFAULT = NO_RECORD;

    private final String name;

    ProbationStatus(String name) {
        this.name = name;
    }

    public static ProbationStatus of(String probationStatus) {
        probationStatus = probationStatus == null ? DEFAULT.name() : probationStatus.toUpperCase();
        try {
            return ProbationStatus.valueOf(probationStatus);
        }
        catch (RuntimeException ex) {
            return ProbationStatus.DEFAULT;
        }
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

}

