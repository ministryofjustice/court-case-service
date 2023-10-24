package uk.gov.justice.probation.courtcaseservice.jpa.entity;


import java.util.Optional;

public enum SourceType {
    LIBRA,
    COMMON_PLATFORM;

    public static SourceType from(final String sourceType) {
        final var source = Optional.ofNullable(sourceType)
            .map(String::trim)
            .map(String::toUpperCase)
            .orElse("");

        if (source.equals("LIBRA")) {
            return LIBRA;
        } else {
            return COMMON_PLATFORM;
        }
    }

}
