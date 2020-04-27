package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentResponse;


@Component
public class AssessmentMapper {
    public Assessment assessmentFrom(AssessmentsApiAssessmentResponse assessmentResponse) {
        return Assessment.builder()
            .type(assessmentResponse.getAssessmentType())
            .completed(assessmentResponse.getCompletedDateTime())
            .build();
    }
}
