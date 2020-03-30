package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel("Offence")
@Getter
@AllArgsConstructor
public class Offence {
    private String description;
}
