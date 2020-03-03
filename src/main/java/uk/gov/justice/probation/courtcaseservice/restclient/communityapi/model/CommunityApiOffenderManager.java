package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CommunityApiOffenderManager {
    @JsonProperty("staff")
    private Staff staff;
    @JsonProperty("fromDate")
    @JsonDeserialize(using = FromDateDeserializer.class)
    private LocalDate fromDate;
}
