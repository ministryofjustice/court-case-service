package uk.gov.justice.probation.courtcaseservice.application;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

import java.util.concurrent.TimeUnit;

@EnableJpaAuditing
@Configuration
public class WebClientConfig {
    @Value("${feature.flags.disable-auth:false}")
    private boolean disableAuthentication;

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;

    @Value("${offender-assessments-api.base-url}")
    private String assessmentsApiBaseUrl;

    @Value("${nomis-oauth.base-url}")
    private String oauthApiBaseUrl;

    @Value("${web.client.connect-timeout-ms}")
    private Integer connectTimeoutMs;

    @Value("${web.client.read-timeout-ms}")
    private long readTimeoutMs;

    @Value("${web.client.write-timeout-ms}")
    private long writeTimeoutMs;

    @Bean
    public RestClientHelper communityApiClient(WebClient communityWebClient) {
        return new RestClientHelper(communityWebClient, "community-api-client", disableAuthentication);
    }

    @Bean
    public RestClientHelper assessmentsApiClient(WebClient assessmentsWebClient) {
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

    @Bean
    public WebClient oauthWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return webClientFactory(oauthApiBaseUrl, authorizedClientManager);
    }

    private WebClient webClientFactory(String baseUrl, OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))));

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}
