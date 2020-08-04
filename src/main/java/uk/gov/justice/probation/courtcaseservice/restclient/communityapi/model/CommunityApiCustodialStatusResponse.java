package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDate;

@ApiModel(description = "Current order header detail response from Community API")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiCustodialStatusResponse {
    @JsonProperty
    private Long sentenceId;
    @JsonProperty
    private KeyValue custodialType;
    @JsonProperty
    private KeyValue sentence;
    @JsonProperty
    private KeyValue mainOffence;
    @JsonProperty
    private LocalDate sentenceDate;
    @JsonProperty
    private LocalDate actualReleaseDate;
    @JsonProperty
    private LocalDate licenceExpiryDate;
    @JsonProperty
    private LocalDate pssEndDate;
    @JsonProperty
    private Integer length;
    @JsonProperty
    private String lengthUnits;
}
