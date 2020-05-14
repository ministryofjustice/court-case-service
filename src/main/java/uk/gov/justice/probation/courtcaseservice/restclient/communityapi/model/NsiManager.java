package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NsiManager {
    private StaffWrapper staff;
    private ProbationArea probationArea;
    private Team team;
    private LocalDate startDate;
}
