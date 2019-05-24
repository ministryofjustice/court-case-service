package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtList {
    private String courtName;
}
