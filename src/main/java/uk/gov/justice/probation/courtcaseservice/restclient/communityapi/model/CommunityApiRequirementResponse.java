package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiRequirementResponse {
    @JsonProperty
    private String rqmntTypeMainCategoryId;
    @JsonProperty
    private String rqmntTypeSubCategoryId;
    @JsonProperty
    private String adRqmntTypeMainCategoryId;
    @JsonProperty
    private String adRqmntTypeSubCategoryId;
    @JsonProperty
    private Integer length;
    @JsonProperty
    @JsonDeserialize(using = CommunityApiDateDeserializer.class)
    private LocalDate startDate;
    @JsonProperty
    @JsonDeserialize(using = CommunityApiDateDeserializer.class)
    private LocalDate terminationDate;
    @JsonProperty
    private String rqmntTerminationReasonId;
}
