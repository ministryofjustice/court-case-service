package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiUnpaidWork {
    @JsonProperty
    private Integer minutesOrdered;
    @JsonProperty
    private Integer minutesCompleted;
    @JsonProperty
    private CommunityApiAppointment appointments;
    @JsonProperty
    private String status;

}
