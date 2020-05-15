package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;

@Service
@AllArgsConstructor
public class BreachService {

    private final NsiRestClient nsiRestClient;
    private final ConvictionRestClient convictionRestClient;
    private final NsiMapper breachMapper;

    public BreachResponse getBreach(String crn, Long convictionId, Long breachId) {
        return Mono.zip(
                nsiRestClient.getNsiById(crn, convictionId, breachId),
                convictionRestClient.getConviction(crn, convictionId)
        )
                .map(tuple -> breachMapper.breachOf(tuple.getT1(), tuple.getT2()))
                .block();
    }
}
