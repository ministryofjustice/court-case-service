package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OffenceResponse {
    private String offenceTitle;
    private String offenceSummary;
    private String act;
}
