package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

@Component
public class OffenderAssessmentsPing implements ReactiveHealthIndicator {
    @Autowired
    @Qualifier("assessmentsApiClient")
    private RestClientHelper assessmentsClient;

    @Override
    public Mono<Health> health() {
        return assessmentsClient.ping();
    }

}