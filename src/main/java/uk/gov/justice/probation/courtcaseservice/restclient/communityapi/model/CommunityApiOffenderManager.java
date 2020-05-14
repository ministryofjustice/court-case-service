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
    private CommunityApiStaff staff;
    @JsonProperty("fromDate")
    @JsonDeserialize(using = CommunityApiDateDeserializer.class)
    private LocalDate fromDate;
}
