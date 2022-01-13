package uk.gov.justice.probation.courtcaseservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockExtension;
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockMockServer;

import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
public abstract class BaseIntTest extends BaseDbTest {

    private static final WiremockMockServer MOCK_SERVER = new WiremockMockServer(WIREMOCK_PORT);

    @RegisterExtension
    static WiremockExtension wiremockExtension = new WiremockExtension(MOCK_SERVER);

    @LocalServerPort
    protected int port;

    @Container
    public GenericContainer postgres = new GenericContainer(DockerImageName.parse("circleci/postgres:11.6-alpine-ram"))
            .withEnv("POSTGRES_USER", "root")
            .withEnv("POSTGRES_PASSWORD", "dev")
            .withExposedPorts(5432);

    @BeforeAll
    public static void setupClass() throws Exception {
        RetryService.tryWireMockStub();
        MOCK_SERVER.start();
    }

    @AfterAll
    public static void afterAll() {
        MOCK_SERVER.stop();
    }

    @BeforeEach
    public void setup() {
        TestConfig.configureRestAssuredForIntTest(port);
    }

}


