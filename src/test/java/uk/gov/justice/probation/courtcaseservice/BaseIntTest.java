package uk.gov.justice.probation.courtcaseservice;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
public class BaseIntTest {
    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .port(WIREMOCK_PORT)
            .usingFilesUnderClasspath("mocks"));

    @LocalServerPort
    protected int port;

    @BeforeClass
    public static void setupClass() throws Exception {
        RetryService.tryWireMockStub();
    }

    @Before
    public void setup() throws Exception {
        TestConfig.configureRestAssuredForIntTest(port);
    }

}


