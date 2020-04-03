package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel("Requirement" )
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Requirement {
    @ApiModelProperty(value = "Unique identifier for the requirement", required = true)
    private Long requirementId;

    private LocalDate commencementDate;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;

    @ApiModelProperty(value = "Is the requirement currently active")
    private boolean active;

    private KeyValue requirementTypeSubCategory;
    private KeyValue requirementTypeMainCategory;
    private KeyValue adRequirementTypeMainCategory;
    private KeyValue adRequirementTypeSubCategory;
    private KeyValue terminationReason;

    private Long length;
    private String lengthUnit;
}
