package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.model.Plea;

import java.time.LocalDate;

@Schema(description = "Offence")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Offence {
    private final String description;
    private final boolean main;
    private final LocalDate offenceDate;
    private final Plea plea;
}
