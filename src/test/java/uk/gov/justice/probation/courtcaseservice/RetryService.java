package uk.gov.justice.probation.courtcaseservice;

import static java.time.Duration.ofSeconds;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryService.class);

    private final WebClient webClient;

    public RetryService() {
        this.webClient = WebClient.create("http://localhost:" + WIREMOCK_PORT + "/");
    }

    /**
     * Calls a simple wiremock stub. It's a workaround to a problem described here.
     * @see <a href="https://github.com/tomakehurst/wiremock/issues/1224">Wiremock Issue</a>
     *
     * @throws Exception
     *              if the method fails to call wiremock stub with 200 status
     */
    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public void tryWireMockStub() throws Exception {
        log.debug("Starting call to ... WireMock readiness stub");
        Optional<ClientResponse> clientResponse = webClient.method(HttpMethod.GET)
                                                            .uri("/readiness/wiremock")
                                                            .exchange()
                                                            .blockOptional(ofSeconds(1));

        if (!clientResponse.map(this::checkCode).orElse(Boolean.FALSE)) {
            log.warn("Call to {} failed.", "/readiness/wiremock");
            throw new Exception("Wiremock not ready!");
        }
        log.debug("Call to WireMock readiness stub completed with response :{}", clientResponse.get().bodyToMono(String.class).block(ofSeconds(1)));
    }

    private boolean checkCode(ClientResponse clientResponse) {
        return clientResponse.statusCode().is2xxSuccessful();
    }
}

