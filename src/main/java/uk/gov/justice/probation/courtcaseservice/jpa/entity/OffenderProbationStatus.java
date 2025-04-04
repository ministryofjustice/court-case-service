package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OffenderProbationStatus {
    CURRENT(DefendantProbationStatus.CURRENT.name()),
    PREVIOUSLY_KNOWN(DefendantProbationStatus.PREVIOUSLY_KNOWN.name()),
    NOT_SENTENCED(DefendantProbationStatus.NOT_SENTENCED.name()),
    UNCONFIRMED_NO_RECORD(DefendantProbationStatus.UNCONFIRMED_NO_RECORD.name()),
    CONFIRMED_NO_RECORD(DefendantProbationStatus.CONFIRMED_NO_RECORD.name());

    private final String name;

    OffenderProbationStatus(String name) {
        this.name = name;
    }

    public static OffenderProbationStatus of(String status) {
        if (status == null) {
            return null;
        }
        return OffenderProbationStatus.valueOf(status.replaceAll(" ", "_").toUpperCase());
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    public DefendantProbationStatus asDefendantProbationStatus() {
        return switch (this) {
            case CURRENT -> DefendantProbationStatus.CURRENT;
            case PREVIOUSLY_KNOWN -> DefendantProbationStatus.PREVIOUSLY_KNOWN;
            case NOT_SENTENCED -> DefendantProbationStatus.NOT_SENTENCED;
            case UNCONFIRMED_NO_RECORD -> DefendantProbationStatus.UNCONFIRMED_NO_RECORD;
            case CONFIRMED_NO_RECORD -> DefendantProbationStatus.CONFIRMED_NO_RECORD;
        };
    }
}

