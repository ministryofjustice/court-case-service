package uk.gov.justice.probation.courtcaseservice.restclient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

@Slf4j
@Component
public class RestClientHelper {

    @Value("${feature.flags.community-api-auth:true}")
    private boolean authenticateWithCommunityApi;

    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    WebClient.RequestHeadersSpec<?> get(final String url) {
        final WebClient.RequestHeadersSpec<?> spec = communityApiClient.get()
                                                                    .uri(url)
                                                                    .accept(MediaType.APPLICATION_JSON);

        if (authenticateWithCommunityApi) {
            log.info(String.format("Authenticating with community api for call to %s", url));
            return spec.attributes(clientRegistrationId("nomis-oauth-client"));
        } else {
            log.info(String.format("Skipping authentication with community api for call to %s", url));
            return spec;
        }
    }

    public Mono<? extends Throwable> handleError(final String crn, final ClientResponse clientResponse) {
        if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) {
            return Mono.error(new OffenderNotFoundException(crn));
        }
        final HttpStatus httpStatus = clientResponse.statusCode();
        throw WebClientResponseException.create(httpStatus.value(),
            httpStatus.name(),
            clientResponse.headers().asHttpHeaders(),
            clientResponse.toString().getBytes(),
            StandardCharsets.UTF_8);
    }
}
