package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CprAddress {

    private String endDate;
    private String postcode;
    private String subBuildingName;
    private String buildingName;
    private String buildingNumber;
    private String thoroughfareName;
    private String dependentLocality;
    private String postTown;
    private String county;
    private String country;
}