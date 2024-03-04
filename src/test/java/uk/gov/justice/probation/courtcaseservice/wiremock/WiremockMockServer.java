package uk.gov.justice.probation.courtcaseservice.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WiremockMockServer extends WireMockServer {

    private static final String DEFAULT_PATH = "mocks";

    public WiremockMockServer(final int port) {
        this(port, DEFAULT_PATH);
    }

    public WiremockMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port)
            .usingFilesUnderClasspath(fileDirectory)
            .notifier(new Slf4jNotifier(true))
            .jettyStopTimeout(10000L));
    }

}
