package uk.gov.justice.probation.courtcaseservice.testUtil;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

public class ConcurrentRequestUtil {
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    public static List<WebTestClient.ResponseSpec> runConcurrentRequests(WebTestClient.RequestBodyUriSpec requestSpec, int requestCount, List<String> ports, String url, Resource caseDetailsExtendedResource)
            throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<WebTestClient.ResponseSpec>> futures = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {

            WebTestClient.RequestBodySpec spec = (WebTestClient.RequestBodySpec) requestSpec
                    .uri(url.replace("{port}", ports.get(i % 2)))
                    .header("authorization", "Bearer " + getToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromResource(caseDetailsExtendedResource));
            Future<WebTestClient.ResponseSpec> future = executor.submit(new SendRequest(spec));
            futures.add(future);
        }
        executor.shutdownNow();

        List<WebTestClient.ResponseSpec> responses = new ArrayList<>();
        for (Future<WebTestClient.ResponseSpec> future : futures) {
            responses.add(future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }

        return responses;
    }

    static class SendRequest implements Callable<WebTestClient.ResponseSpec> {
        private final WebTestClient.RequestBodySpec requestSpec;

        public SendRequest(WebTestClient.RequestBodySpec requestSpec) {
            this.requestSpec = requestSpec;
        }

        @Override
        public WebTestClient.ResponseSpec call() {
            return requestSpec.exchange();
        }
    }
}