package uk.gov.justice.probation.courtcaseservice.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
public class WebClientFactoryIntTest extends BaseIntTest {

    @Value("${web.client.connect-timeout-ms}")
    private Integer connectTimeoutMs;

    @Value("${web.client.read-timeout-ms}")
    private long readTimeoutMs;

    @Value("${web.client.write-timeout-ms}")
    private long writeTimeoutMs;

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;

    @Value("${feature.flags.disable-auth:false}")
    private boolean disableAuthentication;
    @Autowired
    private ClientDetails clientDetails;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    private WebClientFactory webClientFactory;

    @Before
    public void setUp(){
        webClientFactory = new WebClientFactory(connectTimeoutMs, readTimeoutMs, writeTimeoutMs, communityApiBaseUrl, disableAuthentication, clientDetails, clientRegistrationRepository, authorizedClientRepository);
    }

    @Test
    public void shouldReturnWorkingCommunityApiWebClient() {
        final var client = webClientFactory.buildCommunityRestClientHelper();
        final var responseString = client.get("/ping")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(responseString).isEqualTo("pong");
    }

    @Test
    public void shouldReturnWorkingGenericClient() {
        final var client = webClientFactory.buildWebClient(String.format("http://localhost:%s", this.port), 262144);
        final var responseString = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ping")
                            .build()
                    )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(responseString).isEqualTo("pong");
    }
}