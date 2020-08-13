package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiContactDetails {
    private List<CommunityApiAddress> addresses;
}
