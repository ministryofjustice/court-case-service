package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourtList {
    private String courtName;
    private List<Session> sessions;
}
