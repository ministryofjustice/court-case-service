package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Sentence {
    private String description;
    private Integer length;
    private String lengthUnits;
    private Integer lengthInDays;
}
