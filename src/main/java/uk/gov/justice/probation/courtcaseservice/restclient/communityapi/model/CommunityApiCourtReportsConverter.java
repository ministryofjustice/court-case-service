package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CommunityApiCourtReportsConverter extends StdConverter<List<Map<String, Object>>, CommunityApiCourtReportsResponse> {

    private final ObjectMapper objectMapper;

    public CommunityApiCourtReportsConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public CommunityApiCourtReportsResponse convert(List<Map<String, Object>> value) {
        final var list = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiCourtReport.class))
                .collect(Collectors.toList());
        return CommunityApiCourtReportsResponse.builder().courtReports(list).build();
    }
}
