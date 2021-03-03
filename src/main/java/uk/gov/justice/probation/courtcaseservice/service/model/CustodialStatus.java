package uk.gov.justice.probation.courtcaseservice.service.model;

import java.util.stream.Stream;

public enum CustodialStatus {
    SENTENCED_IN_CUSTODY("A"),
    IN_CUSTODY("D"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    TERMINATED("T"),
    POST_SENTENCE_SUPERVISION("P"),
    MIGRATED_DATA("-1"),
    IN_CUSTODY_RoTL("R"),
    IN_CUSTODY_IRC("I"),
    AUTO_TERMINATED("AT"),
    NOT_IN_CUSTODY("NOT_IN_CUSTODY"),
    UNKNOWN("");

    private final String code;

    CustodialStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CustodialStatus fromString(final String text) {
        return Stream.of(CustodialStatus.values())
            .filter(custodialStatus -> custodialStatus.getCode().equalsIgnoreCase(text))
            .findFirst()
            .orElse(null);
    }

}
