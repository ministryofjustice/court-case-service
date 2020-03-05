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
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class OffenderRestClient {

    @Value("${community-api.offender-by-crn-url-template}")
    private String offendersUrlTemplate;
    @Autowired
    private OffenderMapper mapper;
    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    public Mono<Offender> getOffenderByCrn(String crn) {
        return communityApiClient.get()
                .uri(String.format(offendersUrlTemplate, crn))
                .attributes(clientRegistrationId("nomis-oauth-client"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender data for CRN '%s'", crn), e))
                .map(offender -> mapper.offenderFrom(offender));
    }
}
