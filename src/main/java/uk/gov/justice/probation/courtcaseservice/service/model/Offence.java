package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Offence")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Offence {
    private final String description;
    private final boolean main;
    private final LocalDate offenceDate;
}
