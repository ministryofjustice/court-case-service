package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

@ApiModel("Sentence")
@Getter
@Builder
public class Sentence {
    private String description;
    private Integer length;
    private String lengthUnits;
    private Integer lengthInDays;
}
