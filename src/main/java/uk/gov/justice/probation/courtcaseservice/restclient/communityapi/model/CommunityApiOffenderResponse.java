package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CommunityApiOffenderResponse {
    @JsonProperty("otherIds")
    private OtherIds otherIds;
    @JsonProperty("offenderManagers")
    private List<CommunityApiOffenderManager> offenderManagers;
}
