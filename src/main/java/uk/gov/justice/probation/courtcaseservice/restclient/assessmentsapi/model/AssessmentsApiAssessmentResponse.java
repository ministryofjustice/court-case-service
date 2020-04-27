package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentsApiAssessmentResponse {
    @JsonProperty("assessmentType")
    private String assessmentType;

    @JsonProperty("completed")
    private LocalDateTime completedDateTime;
}
