package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum DefendantProbationStatus {
    CURRENT("Current"),
    PREVIOUSLY_KNOWN("Previously known"),
    NOT_SENTENCED("Pre-sentence record"),
    UNCONFIRMED_NO_RECORD("No record"),
    CONFIRMED_NO_RECORD("No record")
    ;

    private static final DefendantProbationStatus DEFAULT = UNCONFIRMED_NO_RECORD;

    private final String name;

    DefendantProbationStatus(String name) {
        this.name = name;
    }

    public static DefendantProbationStatus of(String status) {
        final var probationStatus = status == null ? DEFAULT.name() : status.trim().toUpperCase();
        try {
            return DefendantProbationStatus.valueOf(probationStatus.replaceAll(" ", "_"));
        }
        catch (RuntimeException ex) {
            log.error("Unable to map {} to a known ProbationStatus enum value", status, ex);
            return DefendantProbationStatus.DEFAULT;
        }
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

}

