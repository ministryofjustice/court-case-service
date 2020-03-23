package uk.gov.justice.probation.courtcaseservice.restclient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class RestClientHelper {

    @Value("${feature.flags.community-api-auth:true}")
    private boolean authenticateWithCommunityApi;

    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    public WebClient.RequestHeadersSpec<?> get(final String url) {
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
}
