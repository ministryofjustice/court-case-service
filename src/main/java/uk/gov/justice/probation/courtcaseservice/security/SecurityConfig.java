package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;

@Configuration
public class SecurityConfig {
    @Bean
    @Scope("prototype")
    public OAuth2AuthorizedClientManager authorizedClientManager (
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            ClientDetails clientDetails) {

        var converter = new CustomOAuth2ClientCredentialsGrantRequestEntityConverter();

        var username = clientDetails.getUsername();
        var clientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        clientCredentialsTokenResponseClient.setRequestEntityConverter(grantRequest -> {
            return converter.enhanceWithUsername(grantRequest, username);
        });

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
