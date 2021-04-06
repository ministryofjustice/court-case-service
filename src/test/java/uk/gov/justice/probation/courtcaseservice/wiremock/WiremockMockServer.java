package uk.gov.justice.probation.courtcaseservice.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WiremockMockServer extends WireMockServer {

    private static final String DEFAULT_PATH = "mocks";

    public WiremockMockServer(final int port) {
        this(port, DEFAULT_PATH);
    }

    public WiremockMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port)
            .usingFilesUnderClasspath(fileDirectory)
            .jettyStopTimeout(10000L));
    }

}
