package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Sex {
    FEMALE("F"),
    MALE("M"),
    NOT_KNOWN("N"),
    NOT_SPECIFIED("NS");

    private final String name;

    Sex(final String sex) {
        this.name = sex;
    }

    /**
     * Common platform defines this enum
     *       "enum": [
     *         "MALE",
     *         "FEMALE",
     *         "NOT_KNOWN",
     *         "NOT_SPECIFIED"
     *       ]
     */
    public static Sex fromString(final String sex) {

        switch (Optional.ofNullable(sex).map(s -> s.trim().toUpperCase()).orElse(NOT_KNOWN.name())) {
            case "MALE", "M" -> { return Sex.MALE; }
            case "FEMALE", "F" -> { return Sex.FEMALE; }
            case "NOT_KNOWN", "N" -> { return Sex.NOT_KNOWN; }
            case "NOT_SPECIFIED", "NS" -> { return Sex.NOT_SPECIFIED; }
            default -> {
                log.error("Received an unexpected value to map for sex {}", sex);
                return Sex.NOT_KNOWN;
            }
        }
    }

    public String getName() {
        return name;
    }
}
