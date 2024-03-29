package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OtherIds {
    private final String crn;
    private final String pncNumber;
    private final String croNumber;
    private final String nomsNumber;
}
