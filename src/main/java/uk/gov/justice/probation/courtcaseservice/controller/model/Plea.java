package uk.gov.justice.probation.courtcaseservice.controller.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Plea")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Plea {
    private String  pleaValue;
    private LocalDate pleaDate;
}
