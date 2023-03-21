package uk.gov.justice.probation.courtcaseservice.controller.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity;

import java.time.LocalDate;

@Schema(description = "Verdict")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Verdict {
    private String typeDescription;
    private LocalDate date;
}
