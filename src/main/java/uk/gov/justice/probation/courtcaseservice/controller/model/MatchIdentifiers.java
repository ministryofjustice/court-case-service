package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
public class MatchIdentifiers {
    @NotNull
    private final String crn;
    private final String pnc;
    private final String cro;
}
