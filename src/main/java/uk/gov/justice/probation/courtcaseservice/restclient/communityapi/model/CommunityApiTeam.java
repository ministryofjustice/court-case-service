package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CommunityApiTeam {
    private final String description;
    private final String telephone;
    private final KeyValue localDeliveryUnit;
    private final KeyValue teamType;
    private final KeyValue district;
}
