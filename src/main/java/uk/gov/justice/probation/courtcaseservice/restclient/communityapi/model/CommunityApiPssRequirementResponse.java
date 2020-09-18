package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiPssRequirementResponse {

    private final KeyValue type;
    private final KeyValue subType;
    @ApiModelProperty(value = "Is the requirement currently active")
    private final Boolean active;
}
