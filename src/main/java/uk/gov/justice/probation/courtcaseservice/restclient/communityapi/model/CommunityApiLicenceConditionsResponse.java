package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CommunityApiLicenceConditionsResponse {

    private final List<CommunityApiLicenceCondition> licenceConditions;
}
