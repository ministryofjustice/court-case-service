package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Getter;

@Getter
public class Sentence {
    private String description;
    private Integer length;
    private String lengthUnits;
    private Integer lengthInDays;
}
