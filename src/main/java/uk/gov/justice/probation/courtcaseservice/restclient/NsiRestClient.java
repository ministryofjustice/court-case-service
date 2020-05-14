package uk.gov.justice.probation.courtcaseservice.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;

@Component
public class NsiRestClient {

    @Autowired
    @Qualifier("communityApiClient")
    private RestClientHelper clientHelper;
    private String nsiUrlTemplate = "/secure/offenders/crn/%s/convictions/%s/nsis/%s";

    public Mono<CommunityApiNsi> getNsiById(String crn, Long convictionId, Long nsiId) {
        return clientHelper.get(String.format(nsiUrlTemplate, crn, convictionId, nsiId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleNsiError(crn, convictionId, nsiId, clientResponse))
                .bodyToMono(CommunityApiNsi.class);
    }
}
