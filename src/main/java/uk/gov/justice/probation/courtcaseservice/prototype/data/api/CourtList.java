package uk.gov.justice.probation.courtcaseservice.prototype.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtList {
    private String courtHouse;
    private LocalDate dateOfAppearance;
    private List<Session> sessions;
}
