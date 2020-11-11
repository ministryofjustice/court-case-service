package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Long nsiId;
    private LocalDate actualStartDate;
    private LocalDate referralDate;
    private LocalDateTime statusDateTime;
    private String notes;

    @JsonProperty("nsiType")
    private CommunityApiNsiType type;

    @JsonProperty("nsiSubType")
    private CommunityApiNsiType subType;

    @JsonProperty("nsiStatus")
    private CommunityApiNsiStatus status;

    private List<CommunityApiNsiManager> nsiManagers;
}
