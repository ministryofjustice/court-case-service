package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {


    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;
    @Value("${oauth-server.base-url}")
    private String oauthBaseUrl;
    @Value("${oauth-server.username}")
    private String username;
    @Value("${oauth-server.password}")
    private String password;

    @Bean(name = "communityApiClient")
    public WebClient getCommunityApiClient() {
        return WebClient
                .builder()
                .baseUrl(communityApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean(name = "oauthClient")
    public WebClient getOAuthClient() {
        return WebClient
                .builder()
                .baseUrl(oauthBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .build();
    }
}
