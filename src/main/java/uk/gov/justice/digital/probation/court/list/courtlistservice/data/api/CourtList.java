package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtList {
    private String courtName;
    private List<Session> sessions;
}
