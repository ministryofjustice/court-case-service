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
        AssessmentsApiAssessmentResponse assessment1 = AssessmentsApiAssessmentResponse.builder()
            .assessmentStatus("COMPLETE")
            .assessmentType("type1")
            .completed(dateTime1)
            .build();
        AssessmentsApiAssessmentResponse assessment2 = AssessmentsApiAssessmentResponse.builder()
            .assessmentStatus("LOCKED_INCOMPLETE")
            .assessmentType("type2")
            .completed(dateTime2)
            .build();

        List<Assessment> assessments = AssessmentMapper.assessmentsFrom(new AssessmentsApiAssessmentsResponse(List.of(assessment2, assessment1)));

        assertThat(assessments).hasSize(2);
        assertThat(assessments).contains(Assessment.builder()
                                                .completed(dateTime1)
                                                .type("type1")
                                                .status("COMPLETE")
                                                .build(),
                                        Assessment.builder()
                                                .completed(dateTime2)
                                                .type("type2")
                                                .status("LOCKED_INCOMPLETE")
                                                .build());
    }

    @DisplayName("Null input list gives empty list")
    @Test
    void givenNull_thenReturnEmptyList() {
        List<Assessment> assessments = AssessmentMapper.assessmentsFrom(new AssessmentsApiAssessmentsResponse(null));

        assertThat(assessments).hasSize(0);
    }
}
