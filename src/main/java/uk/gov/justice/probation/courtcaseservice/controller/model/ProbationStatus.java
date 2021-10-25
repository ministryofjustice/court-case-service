package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public static ProbationStatus of(String status) {
        final var probationStatus = status == null ? DEFAULT.name() : status.trim().toUpperCase();
        try {
            return ProbationStatus.valueOf(probationStatus.replaceAll(" ", "_"));
        }
        catch (RuntimeException ex) {
            log.error("Unable to map {} to a known ProbationStatus enum value", status, ex);
            return ProbationStatus.DEFAULT;
        }
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

}

