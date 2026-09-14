package uk.gov.justice.probation.courtcaseservice.service.model;

import java.util.Arrays;

public enum CaseSearchSortFields {
    NEXT_HEARING_DATE("nextHearingDate");

    private final String sortField;

    CaseSearchSortFields(String sortField) {
        this.sortField = sortField;
    }

    public static CaseSearchSortFields bySortFieldIgnoreCase(String input) {
        return Arrays.stream(values())
            .filter(value -> value.sortField.equalsIgnoreCase(input))
            .findFirst()
            .orElse(null);
    }
}
