package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiLicenceCondition {

    private MainCatTypeDetail licenceConditionTypeMainCat;

    @ApiModelProperty(value = "Is the requirement currently active")
    private Boolean active;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MainCatTypeDetail {

        @ApiModelProperty(required = true)
        private String description;

        @ApiModelProperty(required = true)
        private String code;

    }
}
