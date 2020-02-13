package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@RestController
@Slf4j
public class CommunityApiTestController {

    @Value("${community-api.base-url}")
    private String communityApiBaseUrl;
    @Value("${oauth-server.base-url}")
    private String oauthBaseUrl;
    @Value("${oauth-server.username}")
    private String username;
    @Value("${oauth-server.password}")
    private String password;


    private WebClient oauthClient;
    private WebClient communityClient;

    @PostConstruct
    public void initializeClients() {
        oauthClient = WebClient
                .builder()
                .baseUrl(oauthBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.toBase64String((username + ":" + password).getBytes()))
                .build();
        communityClient = WebClient
                .builder()
                .baseUrl(communityApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @GetMapping(value = "/test/community-api-get-token", produces = "application/json")
    public @ResponseBody
    Mono<TokenResponse> courtCase() {

        Mono<ClientResponse> data = oauthClient.post()
                .uri("auth/oauth/token?grant_type=client_credentials&username=AUTH_RO_USER")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();


        Mono<TokenResponse> tokenResponseMono = data.block().bodyToMono(TokenResponse.class);
        return tokenResponseMono;
    }

    @GetMapping(value = "/test/community-api-get-something", produces = "application/json")
    public String something() {

        var authToken = courtCase().block().getAccessToken();

        WebClient.ResponseSpec data = communityClient.get()
                .uri("secure/probationAreas?active=true&excludeEstablishments=true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

        return data.bodyToMono(String.class).block();
    }
}
