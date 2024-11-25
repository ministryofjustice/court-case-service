package uk.gov.justice.probation.courtcaseservice.testUtil;

import org.jmock.lib.concurrent.Blitzer;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

public class ConcurrentRequestUtil {
    public static void blitz(int actionCount, int threadCount, WebTestClient client,
                             List<String> ports, String url, Resource caseDetailsExtendedResource) throws InterruptedException {
        Blitzer blitzer = new Blitzer(actionCount, threadCount);
        try {
            blitzer.blitz(createRunnable( client,  ports, url, caseDetailsExtendedResource));
        } finally {
            blitzer.shutdown();
        }
    }

    private static Runnable createRunnable(WebTestClient client, List<String> ports, String url, Resource caseDetailsExtendedResource){

        return () -> client.put()
                .uri(url.replace("{port}", ports.get(ThreadLocalRandom.current().nextInt(0, 2))))
                .header("authorization", "Bearer " + getToken())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromResource(caseDetailsExtendedResource)).exchange();
    }
    public static void runConcurrentRequests(WebTestClient client, int requestCount, List<String> ports, String url, Resource caseDetailsExtendedResource)
            throws InterruptedException, ExecutionException {
        blitz(requestCount,2,client, ports, url, caseDetailsExtendedResource);
    }
}