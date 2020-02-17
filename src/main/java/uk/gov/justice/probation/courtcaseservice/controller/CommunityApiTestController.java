package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
@Slf4j
public class CommunityApiTestController {
    @Autowired
    @Qualifier("oauthClient")
    private WebClient oauthClient;

    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @GetMapping(value = "/test/community-api-get-token", produces = "application/json")
    public @ResponseBody
    Mono<TokenResponse> getAccessToken() {

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("oauth-server");


        Mono<ClientResponse> data = oauthClient.post()
                .uri("auth/oauth/token?grant_type=client_credentials&username=AUTH_RO_USER")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();


        return data.block().bodyToMono(TokenResponse.class);
    }

    @GetMapping(value = "/test/community-api-get-something", produces = "application/json")
    public String something(Authentication authentication) {

        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("nomis-oauth")
                .principal(new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return "AUTH_RO_USER";
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return "AUTH_RO_USER";
                    }
                })
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(request);

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        WebClient.ResponseSpec data = communityApiClient.get()
                .uri("secure/probationAreas?active=true&excludeEstablishments=true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

        return data.bodyToMono(String.class).block();
    }
}
