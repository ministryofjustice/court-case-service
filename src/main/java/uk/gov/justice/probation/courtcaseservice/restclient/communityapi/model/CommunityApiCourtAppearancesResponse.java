package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CommunityApiCourtAppearancesResponse {
    private final List<CommunityApiCourtAppearanceResponse> courtAppearances;
}
