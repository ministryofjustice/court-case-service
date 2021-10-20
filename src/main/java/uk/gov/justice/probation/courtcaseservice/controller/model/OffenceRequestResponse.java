package uk.gov.justice.probation.courtcaseservice.controller.model;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class OffenceRequestResponse {
    @NotBlank
    private final String offenceTitle;
    @NotBlank
    private final String offenceSummary;
    private final String act;
}
