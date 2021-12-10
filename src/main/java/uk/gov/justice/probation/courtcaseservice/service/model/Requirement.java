package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Requirement" )
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Requirement {
    @Schema(description = "Unique identifier for the requirement", required = true)
    private final Long requirementId;

    private final LocalDate commencementDate;
    private final LocalDate startDate;
    private final LocalDate terminationDate;
    private final LocalDate expectedStartDate;
    private final LocalDate expectedEndDate;

    @Schema(description = "Is the requirement currently active")
    private final boolean active;

    private final KeyValue requirementTypeSubCategory;
    private final KeyValue requirementTypeMainCategory;
    private final KeyValue adRequirementTypeMainCategory;
    private final KeyValue adRequirementTypeSubCategory;
    private final KeyValue terminationReason;

    private final Long length;
    private final String lengthUnit;
}
