package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

@EnableJpaAuditing
@Configuration
public class WebClientConfig {
    @Value("${feature.flags.disable-auth:false}")
    private boolean disableAuthentication;

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;

    @Value("${offender-assessments-api.base-url}")
    private String assessmentsApiBaseUrl;

    @Bean(name = "communityApiClient")
    public RestClientHelper getCommunityApiClient(WebClient communityWebClient) {
        return new RestClientHelper(communityWebClient, "community-api-client", disableAuthentication);
    }

    @Bean(name = "assessmentsApiClient")
    public RestClientHelper getAssessmentsApiClient(WebClient assessmentsWebClient) {
        return new RestClientHelper(assessmentsWebClient, "offender-assessments-api-client", disableAuthentication);
    }

    @Bean
    public WebClient communityWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return webClientFactory(communityApiBaseUrl, authorizedClientManager);
    }

    @Bean
    public WebClient assessmentsWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return webClientFactory(assessmentsApiBaseUrl, authorizedClientManager);
    }

    private WebClient webClientFactory(String baseUrl, OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        return WebClient
                .builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}
