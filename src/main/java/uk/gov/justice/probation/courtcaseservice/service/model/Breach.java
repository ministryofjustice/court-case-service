package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Breach")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Breach {
    private final Long breachId;
    private final String description;
    private final String status;
    private final LocalDate started;
    private final LocalDate statusDate;
}
