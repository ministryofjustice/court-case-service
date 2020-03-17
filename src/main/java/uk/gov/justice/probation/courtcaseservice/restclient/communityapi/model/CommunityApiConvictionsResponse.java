package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(converter = CommunityApiConvictionConverter.class)
public class CommunityApiConvictionsResponse {
    private List<CommunityApiConvictionResponse> convictions;
}
