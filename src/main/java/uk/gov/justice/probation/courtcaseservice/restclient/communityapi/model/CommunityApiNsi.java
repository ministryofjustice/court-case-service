package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiNsi {
    @JsonProperty("nsiId")
    private Long nsiId;

    @JsonProperty("actualStartDate")
    private LocalDate actualStartDate;

    @JsonProperty("referralDate")
    private LocalDate referralDate;

    @JsonProperty("nsiType")
    private CommunityApiNsiType type;

    @JsonProperty("nsiSubType")
    private CommunityApiNsiType subType;

    @JsonProperty("nsiStatus")
    private CommunityApiNsiStatus status;

    @JsonProperty("nsiManagers")
    private List<CommunityApiNsiManager> nsiManagers;
}
