package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
@Slf4j
public class CommunityApiTestController {
    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    @GetMapping(value = "/test/community-api-get-something", produces = "application/json")
    public String something() {

        WebClient.ResponseSpec data = communityApiClient.get()
                .uri("secure/probationAreas?active=true&excludeEstablishments=true")
                .attributes(clientRegistrationId("nomis-oauth-client"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

        return data.bodyToMono(String.class).block();
    }
}
