package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;

@ApiModel("Basic Probation Status Detail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationStatusDetail {
    private final ProbationStatus probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean inBreach;
}
