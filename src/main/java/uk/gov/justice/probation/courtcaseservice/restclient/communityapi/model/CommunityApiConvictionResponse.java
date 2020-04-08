package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiConvictionResponse {
    @JsonProperty
    private String convictionId;
    @JsonProperty
    private Boolean active;
    @JsonProperty
    private LocalDate convictionDate;
    @JsonProperty
    private List<CommunityApiOffence> offences;
    @JsonProperty
    private CommunityApiSentence sentence;
}
