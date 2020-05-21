package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class BreachService {

    private final NsiRestClient nsiRestClient;
    private final ConvictionRestClient convictionRestClient;
    private final NsiMapper breachMapper;
    @Value("#{'${community-api.nsis-filter.codes.breaches}'.split(',')}")
    private List<String> nsiBreachCodes;

    public BreachResponse getBreach(String crn, Long convictionId, Long breachId) {
        return Mono.zip(
                nsiRestClient.getNsiById(crn, convictionId, breachId),
                convictionRestClient.getConviction(crn, convictionId)
        )
                .map(tuple -> {
                    CommunityApiNsi nsi = tuple.getT1();
                    validateBreach(nsi);
                    return breachMapper.breachOf(nsi, tuple.getT2());
                })
                .block();
    }

    private void validateBreach(CommunityApiNsi nsi) {
        if (!nsiBreachCodes.contains(nsi.getType().getCode()))
            throw new NsiNotFoundException(String.format("Breach with id '%s' does not exist", nsi.getNsiId()));
    }
}
