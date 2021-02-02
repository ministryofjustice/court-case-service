package uk.gov.justice.probation.courtcaseservice.service.model;

import java.util.function.Function;

public enum CustodialStatus implements Function<KeyValue, CustodialStatus> {
    SENTENCED_IN_CUSTODY("A"),
    IN_CUSTODY("D"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    TERMINATED("T"),
    POST_SENTENCE_SUPERVISION("P"),
    MIGRATED_DATA("-1"),
    IN_CUSTODY_RoTL("R"),
    IN_CUSTODY_IRC("I"),
    AUTO_TERMINATED("AT");

    private final String code;

    CustodialStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CustodialStatus fromString(String text) {
        for (CustodialStatus b : CustodialStatus.values()) {
            if (b.getCode().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public CustodialStatus apply(KeyValue keyValue) {

        return CustodialStatus.fromString(keyValue.getCode());
    }
}
