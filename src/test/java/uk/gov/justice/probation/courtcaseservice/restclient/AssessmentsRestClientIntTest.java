package uk.gov.justice.probation.courtcaseservice.restclient;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AssessmentsRestClientIntTest extends BaseIntTest {
    private static final String CRN = "X320741";
    private static final String CRN_NOT_FOUND = "X320742";
    private static final String CRN_SERVER_ERROR = "CRNXXX";

    @Autowired
    private AssessmentsRestClient assessmentsRestClient;

    @Test
    void whenGetAssessmentByCrnCalled_thenMakeRestCallToAssessmentsApi() {
        var optionalAssessment = assessmentsRestClient.getAssessmentsByCrn(CRN).blockOptional();

        assertThat(optionalAssessment).isNotEmpty();
        var assessments = optionalAssessment.get();

        var assessment = assessments.stream()
            .filter(ass -> ass.getStatus().equalsIgnoreCase("COMPLETE"))
            .findFirst()
            .orElse(null);

        assertThat(assessment.getType()).isEqualTo("LAYER_1");
        assertThat(assessment.getCompleted()).isEqualTo(LocalDateTime.of(2018, Month.JUNE, 20, 23, 11, 9));
    }

    @Test
    void givenAssessmentDoesNotExist_whenGetAssessmentByCrnCalled_ReturnEmpty() {
        assertThrows(OffenderNotFoundException.class, () ->
            assessmentsRestClient.getAssessmentsByCrn(CRN_NOT_FOUND).blockOptional()
        );
    }

    @Test
    void givenServiceThrowsError_whenGetAssessmentByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            assessmentsRestClient.getAssessmentsByCrn(CRN_SERVER_ERROR).block()
        );
    }
}
