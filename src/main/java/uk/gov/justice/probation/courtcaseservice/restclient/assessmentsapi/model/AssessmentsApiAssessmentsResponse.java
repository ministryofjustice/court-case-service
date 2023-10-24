package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class AssessmentsApiAssessmentsResponse {
    private final List<AssessmentsApiAssessmentResponse> timeline;
}
