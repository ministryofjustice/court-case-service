package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;

@Component
public class NsiMapper {
    public BreachResponse breachOf(CommunityApiNsi nsi) {
        return BreachResponse.builder().build();
    }
}
