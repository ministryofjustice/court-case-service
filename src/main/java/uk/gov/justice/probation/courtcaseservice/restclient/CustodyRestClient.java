package uk.gov.justice.probation.courtcaseservice.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentencesResponse;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CustodyMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

@Component
public class CustodyRestClient {
    @Value("${prison-api.sentences-template}")
    private String sentencesTemplate;

    @Autowired
    @Qualifier("prisonApiClient")
    private RestClientHelper clientHelper;

    public Mono<Custody> getCustody(String nomsNumber) {

        final String path = String.format(sentencesTemplate, nomsNumber);
        return clientHelper.get(path)
                .retrieve()
                .bodyToMono(PrisonApiSentencesResponse.class)
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == HttpStatus.NOT_FOUND.value() ? Mono.empty() : Mono.error(ex))
                .map(CustodyMapper::custodyFrom);
    }
}
