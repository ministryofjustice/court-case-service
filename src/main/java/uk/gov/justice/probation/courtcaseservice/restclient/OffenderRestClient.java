package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import java.util.List;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class OffenderRestClient {
    @Value("${community-api.offender-by-crn-url-template}")
    private String offenderUrlTemplate;
    @Value("${community-api.convictions-by-crn-url-template}")
    private String convictionsUrlTemplate;
    @Autowired
    private OffenderMapper mapper;
    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    public Mono<Offender> getOffenderByCrn(String crn) {
        return communityApiClient.get()
                .uri(String.format(offenderUrlTemplate, crn))
                // TODO: Fix auth issue when running test profile against Wiremock
//                .attributes(clientRegistrationId("nomis-oauth-client"))
                .header("Authorization", "Bearer foo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender data for CRN '%s'", crn), e))
                .map(offender -> mapper.offenderFrom(offender));
    }

    public Mono<List<Conviction>> getConvictionsByCrn(String crn) {
        return communityApiClient.get()
                .uri(String.format(convictionsUrlTemplate, crn))
                // TODO: Fix auth issue when running test profile against Wiremock
//                .attributes(clientRegistrationId("nomis-oauth-client"))
                .header("Authorization", "Bearer foo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiConvictionsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving convictions data for CRN '%s'", crn), e))
                .map( convictionsResponse -> mapper.convictionsFrom(convictionsResponse));
    }
}
