package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
public class CommunityApiTestController {

    WebClient oauthClient = WebClient
            .builder()
            .baseUrl("http://localhost:8095")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic bGljZW5jZXNhZG1pbjpjbGllbnRzZWNyZXQ=")
            .build();

    @GetMapping(value = "/test/community-api-get-token", produces = "application/json")
    public Flux<String> courtCase() {

        WebClient.ResponseSpec data = oauthClient.post()
                .uri("auth/oauth/token?grant_type=client_credentials&username=AUTH_RO_USER")
                .body(BodyInserters.fromObject("data")).retrieve();

        return data.bodyToFlux(String.class);
    }
}
