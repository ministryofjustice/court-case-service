package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiStaff {
    private String forenames;
    private String surname;
    private String email;
    private String phoneNumber;
}
