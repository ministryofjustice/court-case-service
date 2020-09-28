package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exercises AssessmentMapper")
class AssessmentMapperTest {

    @DisplayName("Simple case of mapping multiple assessments")
    @Test
    void givenMultipleAssessments_thenReturn() {
        LocalDateTime dateTime1 = LocalDateTime.now();
        LocalDateTime dateTime2 = LocalDateTime.now().minusHours(1);
        AssessmentsApiAssessmentResponse assessment1 = new AssessmentsApiAssessmentResponse("type1", dateTime1);
        AssessmentsApiAssessmentResponse assessment2 = new AssessmentsApiAssessmentResponse("type2", dateTime2);

        List<Assessment> assessments = AssessmentMapper.assessmentsFrom(new AssessmentsApiAssessmentsResponse(List.of(assessment2, assessment1)));

        assertThat(assessments).hasSize(2);
        assertThat(assessments).contains(Assessment.builder().completed(dateTime1).type("type1").build(),
                                        Assessment.builder().completed(dateTime2).type("type2").build());
    }

    @DisplayName("Null input list gives empty list")
    @Test
    void givenNull_thenReturnEmptyList() {
        List<Assessment> assessments = AssessmentMapper.assessmentsFrom(new AssessmentsApiAssessmentsResponse(null));

        assertThat(assessments).hasSize(0);
    }
}
