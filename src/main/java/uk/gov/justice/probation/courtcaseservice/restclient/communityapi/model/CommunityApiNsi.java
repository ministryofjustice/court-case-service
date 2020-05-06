package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiNsi {
    @JsonProperty("nsiId")
    private Long nsiId;

    @JsonProperty("actualStartDate")
    private LocalDate actualStartDate;

    @JsonProperty("nsiType")
    private CommunityApiNsiType type;

    @JsonProperty("nsiSubType")
    private CommunityApiNsiType subType;

    @JsonProperty("nsiStatus")
    private CommunityApiNsiStatus status;
}
