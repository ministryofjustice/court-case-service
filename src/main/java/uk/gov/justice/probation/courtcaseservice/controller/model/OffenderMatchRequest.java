package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderMatchRequest {
    @NotNull
    private final MatchIdentifiers matchIdentifiers;
    @NotNull
    private final MatchType matchType;
    @NotNull
    private final Boolean confirmed;

    public OffenderMatchEntity asMatchForCase(CourtCaseResponse courtCase) {
        return new OffenderMatchEntity();
    }
}
