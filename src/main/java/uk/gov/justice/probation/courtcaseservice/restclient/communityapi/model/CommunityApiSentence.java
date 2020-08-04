package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiSentence {
    @JsonProperty
    private Long sentenceId;
    @JsonProperty
    private String description;
    @JsonProperty
    private Integer originalLength;
    @JsonProperty
    private String originalLengthUnits;
    @JsonProperty
    private Integer lengthInDays;
    @JsonProperty
    private LocalDate terminationDate;
    @JsonProperty
    private String terminationReason;
    @JsonProperty
    private CommunityApiUnpaidWork unpaidWork;
}
