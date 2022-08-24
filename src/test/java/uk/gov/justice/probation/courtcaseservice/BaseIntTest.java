package uk.gov.justice.probation.courtcaseservice;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import uk.gov.justice.probation.courtcaseservice.controller.CCSPostgresqlContainer;
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockExtension;
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockMockServer;

import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
public abstract class BaseIntTest {

    private static final WiremockMockServer MOCK_SERVER = new WiremockMockServer(WIREMOCK_PORT);

    @RegisterExtension
    static WiremockExtension wiremockExtension = new WiremockExtension(MOCK_SERVER);

    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.17.3");

    @LocalServerPort
    protected int port;

    @Container
    public static PostgreSQLContainer postgresqlContainer = CCSPostgresqlContainer.getInstance();

    @ClassRule
    public static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(
                    LocalStackContainer.Service.SNS,
                    LocalStackContainer.EnabledService.named("events")
            );


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


