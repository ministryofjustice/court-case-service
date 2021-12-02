package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityApiCourtReportsConverter extends StdConverter<List<Map<String, Object>>, CommunityApiCourtReportsResponse> {

    private final ObjectMapper objectMapper;

    public CommunityApiCourtReportsConverter() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Override
    public CommunityApiCourtReportsResponse convert(List<Map<String, Object>> value) {
        final var list = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiCourtReport.class))
                .collect(Collectors.toList());
        return CommunityApiCourtReportsResponse.builder().courtReports(list).build();
    }
}
