package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class MatchIdentifiers {
    @NotNull
    private final String crn;
    private final String pnc;
    private final String cro;
}
