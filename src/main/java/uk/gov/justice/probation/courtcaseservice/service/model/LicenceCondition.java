package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel("Licence Condition")
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LicenceCondition {

    private final String description;
    private final String subTypeDescription;
    private final LocalDate startDate;
    private final String notes;

    @JsonIgnore
    private final boolean active;
}
