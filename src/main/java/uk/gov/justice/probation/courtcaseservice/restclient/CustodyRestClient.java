package uk.gov.justice.probation.courtcaseservice.restclient;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

@Component
public class CustodyRestClient {
    public Mono<Custody> getCustody(String nomsNumber) {
        return null;
    }
}
