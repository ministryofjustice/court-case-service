package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityApiNsiManager {
    private CommunityApiStaffWrapper staff;
    private CommunityApiProbationArea probationArea;
    private CommunityApiTeam team;
    private LocalDate startDate;
}
