package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

@ApiModel("Breach")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Breach {
    private Long id;
    private String description;
    private String status;
    private LocalDate started;
}
