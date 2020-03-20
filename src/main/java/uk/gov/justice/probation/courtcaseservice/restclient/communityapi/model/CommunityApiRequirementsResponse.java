package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(converter = CommunityApiRequirementConverter.class)
public class CommunityApiRequirementsResponse {
    private List<CommunityApiRequirementResponse> requirements;
}
