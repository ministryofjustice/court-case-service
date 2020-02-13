package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class CommunityApiTestController {

    private String username = "community-api-client";
    private String password = "clientsecret";

    WebClient oauthClient = WebClient
            .builder()
            .baseUrl("http://localhost:8095")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.toBase64String((username + ":" + password).getBytes()))
            .build();

    WebClient communityClient = WebClient
            .builder()
            .baseUrl("http://localhost:8096")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    @GetMapping(value = "/test/community-api-get-token", produces = "application/json")
    public @ResponseBody
    Mono<TokenResponse> courtCase() {

        Mono<ClientResponse> data = oauthClient.post()
                .uri("auth/oauth/token?grant_type=client_credentials&username=AUTH_RO_USER")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();


        return data.block().bodyToMono(TokenResponse.class);
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
