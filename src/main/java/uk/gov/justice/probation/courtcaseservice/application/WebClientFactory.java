package uk.gov.justice.probation.courtcaseservice.application;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;
import uk.gov.justice.probation.courtcaseservice.security.UserAwareEntityConverter;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class WebClientFactory {
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 262144;

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

    public RestClientHelper buildCommunityRestClientHelper(@Nullable String username) {
        final var webClient = buildWebClient(communityApiBaseUrl, DEFAULT_BYTE_BUFFER_SIZE, username);
        return new RestClientHelper(webClient, "community-api-client", disableAuthentication);
    }

    public WebClient buildWebClient(String baseUrl, int bufferByteCount) {
        return buildWebClient(baseUrl, bufferByteCount, null);
    }

    public WebClient buildWebClient(String baseUrl, int bufferByteCount, @Nullable String username) {
        var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(buildAuthorizedClientManager(username));

        var httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))));

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferByteCount))
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    private OAuth2AuthorizedClientManager buildAuthorizedClientManager(@Nullable String username) {

        var converter = new UserAwareEntityConverter();

        var clientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        clientCredentialsTokenResponseClient.setRequestEntityConverter(
                grantRequest -> converter.enhanceWithUsername(grantRequest, username)
        );

        var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder ->
                        builder.accessTokenResponseClient(clientCredentialsTokenResponseClient))
                .build();

        var authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
