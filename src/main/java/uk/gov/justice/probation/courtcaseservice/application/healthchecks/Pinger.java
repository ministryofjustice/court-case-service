package uk.gov.justice.probation.courtcaseservice.application.healthchecks;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class Pinger {

    @Value("${health.default-ping-path}")
    private String path;

    public Mono<Health> ping(WebClient webClient) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .build()
                )
                .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)
                .exchange()
                .map(response -> {
                    if(response.statusCode().is2xxSuccessful()) {
                        return new Health.Builder().up().build();
                    } else {
                        return Health.down().withDetail("httpStatus", response.statusCode().toString()).build();
                    }

                })
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()));
    }
}
