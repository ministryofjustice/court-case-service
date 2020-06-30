package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderMatchRequest {
    @NotNull
    @Valid
    private final MatchIdentifiers matchIdentifiers;
    @NotNull
    private final MatchType matchType;
    @NotNull
    private final Boolean confirmed;
}
