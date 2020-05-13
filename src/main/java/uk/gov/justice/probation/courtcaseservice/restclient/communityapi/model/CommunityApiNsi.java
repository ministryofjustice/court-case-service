package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityApiNsi {
    private Long nsiId;
    private LocalDate referralDate;
    private LocalDate actualStartDate;
    private List<NsiManager> nsiManagers;
    private NsiStatus nsiStatus;
}
