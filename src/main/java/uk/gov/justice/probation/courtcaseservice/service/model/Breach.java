package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel("Breach")
@Getter
@Builder
public class Breach {
    private String id;
    private String description;
    private String status;
    private LocalDate started;
}
