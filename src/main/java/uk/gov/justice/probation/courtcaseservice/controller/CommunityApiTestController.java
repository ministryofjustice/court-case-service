package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Autowired
    @Qualifier("oauthClient")
    private WebClient oauthClient;

    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

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

        WebClient.ResponseSpec data = communityApiClient.get()
                .uri("secure/probationAreas?active=true&excludeEstablishments=true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

        return data.bodyToMono(String.class).block();
    }
}
