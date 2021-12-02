package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityApiOffenderManagersConverter extends StdConverter<List<Map<String, Object>>, CommunityApiCommunityOrPrisonOffenderManagerResponse> {

    private final ObjectMapper objectMapper;

    public CommunityApiOffenderManagersConverter() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Override
    public CommunityApiCommunityOrPrisonOffenderManagerResponse convert(List<Map<String, Object>> value) {
        List<CommunityApiCommunityOrPrisonOffenderManager> list = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiCommunityOrPrisonOffenderManager.class))
                .collect(Collectors.toList());
        return CommunityApiCommunityOrPrisonOffenderManagerResponse.builder().offenderManagers(list).build();
    }
}
