package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiOffenderResponse {
    private String offenderId;
    @JsonProperty("otherIds")
    private OtherIds otherIds;
    @JsonProperty("offenderManagers")
    private List<CommunityApiOffenderManager> offenderManagers;
    private String title;
    private String firstName;
    private List<String> middleNames;
    private String surname;
    private LocalDate dateOfBirth;
    private CommunityApiContactDetails contactDetails;
    @JsonDeserialize(using = NumericBooleanDeserializer.class)
    private boolean currentDisposal;

}
