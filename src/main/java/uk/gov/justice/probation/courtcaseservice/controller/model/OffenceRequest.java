package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OffenceRequest {
    private final String offenceTitle;
    private final String offenceSummary;
    private final String act;
}
