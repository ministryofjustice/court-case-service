package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;
import uk.gov.justice.probation.courtcaseservice.security.WebClientFactory;

@EnableJpaAuditing
@Configuration
public class WebClientConfig {

    /**
     * Size of buffer by default.
     * @see org.springframework.core.codec.AbstractDataBufferDecoder
     * */
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 262144;

    @Value("${feature.flags.disable-auth:false}")
    private boolean disableAuthentication;

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;

    @Value("${offender-assessments-api.base-url}")
    private String assessmentsApiBaseUrl;

    @Value("${nomis-oauth.base-url}")
    private String oauthApiBaseUrl;

    @Value("${web.client.document-byte-buffer-size}")
    private int documentBufferByteSize;

    @Bean
    public RestClientHelper documentApiClient(WebClient documentWebClient) {
        return new RestClientHelper(documentWebClient, "community-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper communityApiClient(WebClient communityWebClient) {
        return new RestClientHelper(communityWebClient, "community-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper assessmentsApiClient(WebClient assessmentsWebClient) {
        return new RestClientHelper(assessmentsWebClient, "offender-assessments-api-client", disableAuthentication);
    }

    @Bean
    public WebClient documentWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(communityApiBaseUrl, documentBufferByteSize);
    }

    @Bean
    public WebClient communityWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(communityApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient assessmentsWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(assessmentsApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient oauthWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(oauthApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

}
