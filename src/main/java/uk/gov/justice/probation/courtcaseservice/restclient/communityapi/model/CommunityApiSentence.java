package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiSentence {
    @JsonProperty
    private String description;
    @JsonProperty
    private Integer originalLength;
    @JsonProperty
    private String originalLengthUnits;
    @JsonProperty
    private Integer lengthInDays;
}
