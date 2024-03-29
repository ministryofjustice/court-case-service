package uk.gov.justice.probation.courtcaseservice.application.healthchecks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OffenceApi implements ReactiveHealthIndicator {

    @Autowired
    @Qualifier("manageOffencesApiWebClient")
    private WebClient manageOffencesApiWebClient;
    @Autowired
    private Pinger pinger;

    @Override
    public Mono<Health> health() {
        return pinger.ping(manageOffencesApiWebClient);
    }
}
