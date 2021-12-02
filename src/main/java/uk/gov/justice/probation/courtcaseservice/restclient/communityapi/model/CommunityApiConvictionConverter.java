package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityApiConvictionConverter extends StdConverter<List<Map<String, Object>>, CommunityApiConvictionsResponse> {

    private final ObjectMapper objectMapper;

    public CommunityApiConvictionConverter() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Override
    public CommunityApiConvictionsResponse convert(List<Map<String, Object>> value) {
        List<CommunityApiConvictionResponse> convictionsList = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiConvictionResponse.class))
                .collect(Collectors.toList());
        return new CommunityApiConvictionsResponse(convictionsList);
    }
}
