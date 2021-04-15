package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonDeserialize(converter = CommunityApiOffenderManagersConverter.class)
public class CommunityApiCommunityOrPrisonOffenderManagerResponse {
    private List<CommunityApiCommunityOrPrisonOffenderManager> offenderManagers;
}
