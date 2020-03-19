package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityApiRequirementConverter extends StdConverter<List<Map<String, Object>>, CommunityApiRequirementsResponse> {

    @Override
    public CommunityApiRequirementsResponse convert(List<Map<String, Object>> value) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CommunityApiRequirementResponse> requirementsList = value.stream()
                .map(map -> objectMapper.convertValue(map, CommunityApiRequirementResponse.class))
                .collect(Collectors.toList());
        return new CommunityApiRequirementsResponse(requirementsList);
    }
}
