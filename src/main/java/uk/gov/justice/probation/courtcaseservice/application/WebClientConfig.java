package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

@Configuration
public class WebClientConfig {

    /**
     * Size of buffer by default.
     * @see AbstractDataBufferDecoder
     * */
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 262144;

    @Value("${feature.flags.disable-auth:false}")
    private boolean disableAuthentication;

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;

    @Value("${offender-assessments-api.base-url}")
    private String assessmentsApiBaseUrl;

    @Value("${prison-api.base-url}")
    private String prisonApiBaseUrl;

    @Value("${nomis-oauth.base-url}")
    private String oauthApiBaseUrl;

    @Value("${manage-offences-api.base-url}")
    private String manageOffencesApiBaseUrl;

    @Value("${court-case-and-delius-api.base-url}")
    private String courtCaseDeliusApiBaseUrl;

    @Value("${domain-event-and-delius-api.base-url}")
    private String domainEventAndDeliusApiBaseUrl;

    @Value("${hmpps-document-management-api-client.base-url}")
    private String hmppsDocumentManagementApiUrl;

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
    public RestClientHelper prisonApiClient(WebClient prisonApiWebClient) {
        return new RestClientHelper(prisonApiWebClient, "prison-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper manageOffencesApiClient(WebClient manageOffencesApiWebClient) {
        return new RestClientHelper(manageOffencesApiWebClient, "manage-offences-api-client", disableAuthentication);
    }

    @Bean
    @Qualifier("hmppsDocumentManagementApiRestClient")
    public RestClientHelper hmppsDocumentManagementApiRestClient(WebClient hmppsDocumentManagementApiWebClient) {
        return new RestClientHelper(hmppsDocumentManagementApiWebClient, "hmpps-document-management-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper courtCaseDeliusApiClient(WebClient courtCaseDeliusApiWebClient) {
        return new RestClientHelper(courtCaseDeliusApiWebClient, "court-case-and-delius-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper domainEventAndDeliusApiClient(WebClient domainEventAndDeliusApiWebClient) {
        return new RestClientHelper(domainEventAndDeliusApiWebClient, "domain-event-and-delius-api-client", disableAuthentication);
    }

    @Bean
    public WebClient documentWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(communityApiBaseUrl, documentBufferByteSize);
    }

    @Bean
    public WebClient communityWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(communityApiBaseUrl, 2 * DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient assessmentsWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(assessmentsApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient prisonApiWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(prisonApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient oauthWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(oauthApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient manageOffencesApiWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(manageOffencesApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient courtCaseDeliusApiWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(courtCaseDeliusApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient domainEventAndDeliusApiWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(domainEventAndDeliusApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE);
    }

    @Bean
    public WebClient hmppsDocumentManagementApiWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.buildWebClient(hmppsDocumentManagementApiUrl, 17 * DEFAULT_BYTE_BUFFER_SIZE);
    }

}
