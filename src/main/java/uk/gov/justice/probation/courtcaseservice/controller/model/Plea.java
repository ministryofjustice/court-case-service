package uk.gov.justice.probation.courtcaseservice.controller.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity;

import java.time.LocalDate;

@Schema(description = "Plea")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Plea {
    private String value;
    private LocalDate date;
}
