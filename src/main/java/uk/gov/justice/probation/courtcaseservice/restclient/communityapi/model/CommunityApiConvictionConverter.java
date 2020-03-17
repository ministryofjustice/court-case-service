package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityApiConvictionConverter extends StdConverter<List<Map<String, Object>>, CommunityApiConvictionsResponse> {

    @Override
    public CommunityApiConvictionsResponse convert(List<Map<String, Object>> value) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CommunityApiConvictionResponse> convictionsList = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiConvictionResponse.class))
                .collect(Collectors.toList());
        return new CommunityApiConvictionsResponse(convictionsList);
    }
}
