package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class AssessmentsApiAssessmentResponse {
    private final String assessmentType;
    private final LocalDateTime completed;
    private final String assessmentStatus;
}
