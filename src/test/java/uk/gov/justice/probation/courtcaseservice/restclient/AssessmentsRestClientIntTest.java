package uk.gov.justice.probation.courtcaseservice.restclient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import uk.gov.justice.probation.courtcaseservice.TestConfig;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AssessmentsRestClientIntTest {
    private static final String CRN = "X320741";
    private static final String CRN_NOT_FOUND = "X320742";
    private static final String CRN_SERVER_ERROR = "CRNXXX";

    @Autowired
    private AssessmentsRestClient assessmentsRestClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(TestConfig.WIREMOCK_PORT)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenGetAssessmentByCrnCalled_thenMakeRestCallToAssessmentsApi() {
        var optionalAssessment = assessmentsRestClient.getAssessmentByCrn(CRN).blockOptional();

        assertThat(optionalAssessment).isNotEmpty();
        var assessment = optionalAssessment.get();
        assertThat(assessment.getType()).isEqualTo("LAYER_3");
        assertThat(assessment.getCompleted()).isEqualTo(LocalDateTime.of(2018, 6, 20, 23, 0, 9));
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenAssessmentDoesNotExist_whenGetAssessmentByCrnCalled_ReturnEmpty() {
        assessmentsRestClient.getAssessmentByCrn(CRN_NOT_FOUND).blockOptional();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetAssessmentByCrnCalled_thenFailFastAndThrowException() {
        assessmentsRestClient.getAssessmentByCrn(CRN_SERVER_ERROR).block();
    }
}
