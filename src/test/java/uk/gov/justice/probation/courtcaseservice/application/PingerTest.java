package uk.gov.justice.probation.courtcaseservice.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.health.Health;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.application.healthchecks.Pinger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@RunWith(SpringRunner.class)
public class PingerTest extends BaseIntTest {

    @Test
    public void when200_thenUp() {
        Pinger pinger = new Pinger("/ping");
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + WIREMOCK_PORT)
                .build();

        Health health = pinger.ping(webClient)
                .block();

        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    public void when500_thenDown() {
        Pinger pinger = new Pinger("/pingbad");
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + WIREMOCK_PORT)
                .build();

        Health health = pinger.ping(webClient)
                .block();

        assertThat(health.getStatus()).isEqualTo(DOWN);
        assertThat(health.getDetails().get("httpStatus")).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }

    @Test
    public void whenError_thenDown() {
        Pinger pinger = new Pinger("/ping");
        WebClient webClient = WebClient.builder()
                .baseUrl("http://notarealhost")
                .build();

        Health health = pinger.ping(webClient)
                .block();

        assertThat(health.getStatus()).isEqualTo(DOWN);
        assertThat(health.getDetails().get("error")).isEqualTo("java.net.UnknownHostException: notarealhost: nodename nor servname provided, or not known");
    }
}