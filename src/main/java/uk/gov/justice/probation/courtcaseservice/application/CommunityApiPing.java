package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

@Component
public class CommunityApiPing implements ReactiveHealthIndicator {
    @Autowired
    @Qualifier("communityApiClient")
    private RestClientHelper communityApiClient;

    @Override
    public Mono<Health> health() {
        return communityApiClient.ping();
    }

}