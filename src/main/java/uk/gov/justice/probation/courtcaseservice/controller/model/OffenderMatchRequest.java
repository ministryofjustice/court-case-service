package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

@AllArgsConstructor
public class OffenderMatchRequest {
    private final MatchIdentifiers matchIdentifiers;
    private final MatchType matchType;
    private final boolean confirmed;

    public OffenderMatchEntity asMatchForCase(CourtCaseResponse courtCase) {
        return new OffenderMatchEntity();
    }
}
