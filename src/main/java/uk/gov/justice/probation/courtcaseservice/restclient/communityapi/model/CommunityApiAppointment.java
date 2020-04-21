package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiAppointment {
    @JsonProperty
    private Integer total;
    @JsonProperty
    private Integer attended;
    @JsonProperty
    private Integer acceptableAbsences;
    @JsonProperty
    private Integer unacceptableAbsences;
}
