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
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class OffenderRestClient {
    @Value("${community-api.offender-by-crn-url-template}")
    private String offenderUrlTemplate;
    @Value("${community-api.convictions-by-crn-url-template}")
    private String convictionsUrlTemplate;
    @Value("${community-api.requirements-by-crn-url-template}")
    private String requirementsUrlTemplate;
    @Autowired
    private OffenderMapper mapper;
    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;
    @Value("${feature-flags.community-api-auth:true}")
    private boolean authenticateWithCommunityApi;

    public Mono<Offender> getOffenderByCrn(String crn) {
        return get(String.format(offenderUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender data for CRN '%s'", crn), e))
                .map(offender -> mapper.offenderFrom(offender));
    }

    public Mono<List<Conviction>> getConvictionsByCrn(String crn) {
        return get(String.format(convictionsUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiConvictionsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving convictions data for CRN '%s'", crn), e))
                .map( convictionsResponse -> mapper.convictionsFrom(convictionsResponse));
    }

    public Mono<List<Requirement>> getConvictionRequirements(String crn, String convictionId) {
        return get(String.format(requirementsUrlTemplate, crn, convictionId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiRequirementsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving requirements data for CONVICTIONID '%s'", convictionId), e))
                .map( requirementsResponse -> mapper.requirementsFrom(requirementsResponse));
    }

    private WebClient.RequestHeadersSpec<?> get(String url) {
        WebClient.RequestHeadersSpec<?> spec = communityApiClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON);

        if(authenticateWithCommunityApi) {
            log.info(String.format("Authenticating with community api for call to %s", url));
            return spec.attributes(clientRegistrationId("nomis-oauth-client"));
        }
        else {
            log.info(String.format("Skipping authentication with community api for call to %s", url));
            return spec;
        }
    }
}
