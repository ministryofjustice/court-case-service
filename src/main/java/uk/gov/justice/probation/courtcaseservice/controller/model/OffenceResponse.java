package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
public class OffenceResponse {
    private final String offenceTitle;
    private final String offenceSummary;
    private final String act;
    private final int sequenceNumber;

}
