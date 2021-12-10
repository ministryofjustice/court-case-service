package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiAddress {
    @Schema(required = true)
    private LocalDate from;
    private LocalDate to;
    private Boolean noFixedAbode;
    private String notes;
    private String addressNumber;
    private String buildingName;
    private String streetName;
    private String district;
    private String town;
    private String county;
    private String postcode;
    private String telephoneNumber;
    @JsonProperty(value = "status")
    private CommunityApiAddressTypeDetail addressTypeDetail;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommunityApiAddressTypeDetail {

        @Schema(required = true)
        private String description;

        @Schema(required = true)
        private String code;

    }
}
