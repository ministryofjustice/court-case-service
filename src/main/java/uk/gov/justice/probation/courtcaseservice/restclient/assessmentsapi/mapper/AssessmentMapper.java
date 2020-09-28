package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;

public class AssessmentMapper {

    public static List<Assessment> assessmentsFrom(AssessmentsApiAssessmentsResponse assessmentsResponse) {
        return Optional.ofNullable(assessmentsResponse.getAssessments())
            .orElse(Collections.emptyList())
            .stream()
            .map(AssessmentMapper::assessmentFrom)
            .collect(Collectors.toList());
    }

    static Assessment assessmentFrom(AssessmentsApiAssessmentResponse assessmentResponse) {
        return Assessment.builder()
            .type(assessmentResponse.getAssessmentType())
            .completed(assessmentResponse.getCompleted())
            .build();
    }
}
