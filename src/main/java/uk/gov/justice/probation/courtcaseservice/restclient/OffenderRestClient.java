package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class OffenderRestClient {

    @Value("${community-api.offender-by-crn-url-template}")
    private String offendersUrlTemplate;
    @Autowired
    private OffenderMapper mapper;
    @Autowired
    @Qualifier("communityApiClient")
    private WebClient communityApiClient;

    public Offender getOffenderByCrn(String crn) {
        WebClient.ResponseSpec responseSpec = communityApiClient.get()
                .uri(String.format(offendersUrlTemplate, crn))
                .attributes(clientRegistrationId("nomis-oauth-client"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

        CommunityApiOffenderResponse offenderResponse = responseSpec.bodyToMono(CommunityApiOffenderResponse.class).block();
        // TODO: Error handling

        return mapper.offenderFrom(offenderResponse);
    }

}
