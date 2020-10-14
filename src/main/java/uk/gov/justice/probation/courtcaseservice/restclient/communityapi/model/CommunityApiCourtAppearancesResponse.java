package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(description = "Court Appearances from Community API")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityApiCourtAppearancesResponse {
    private final List<CommunityApiCourtAppearanceResponse> courtAppearances;
}
