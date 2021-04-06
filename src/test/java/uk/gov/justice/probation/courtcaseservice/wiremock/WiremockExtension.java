package uk.gov.justice.probation.courtcaseservice.wiremock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WiremockExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private final WiremockMockServer wiremockServer;

    public WiremockExtension(WiremockMockServer server) {
        this.wiremockServer = server;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        wiremockServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        wiremockServer.stop();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        wiremockServer.resetRequests();
    }

}
