package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiConvictionResponse {
    @JsonProperty
    private String convictionId;
    @JsonProperty
    private Boolean active;
    @JsonProperty
    @JsonDeserialize(using = CommunityApiDateDeserializer.class)
    private LocalDate convictionDate;
    @JsonProperty
    private List<CommunityApiOffence> offences;
}
