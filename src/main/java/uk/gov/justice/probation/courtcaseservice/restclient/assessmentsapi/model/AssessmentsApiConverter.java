package uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssessmentsApiConverter extends StdConverter<List<Map<String, Object>>, AssessmentsApiAssessmentsResponse> {

    private final ObjectMapper objectMapper;

    public AssessmentsApiConverter() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Override
    public AssessmentsApiAssessmentsResponse convert(List<Map<String, Object>> value) {
        List<AssessmentsApiAssessmentResponse> list = value.stream()
                .map(map -> objectMapper.convertValue(map, AssessmentsApiAssessmentResponse.class))
                .collect(Collectors.toList());
        return new AssessmentsApiAssessmentsResponse(list);
    }
}
