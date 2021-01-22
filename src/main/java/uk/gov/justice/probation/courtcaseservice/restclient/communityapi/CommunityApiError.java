package uk.gov.justice.probation.courtcaseservice.restclient.communityapi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiError {
    private Integer status;
    private String developerMessage;
}
