package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CommunityApiProbationStatusDetail {
    private final String status;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean preSentenceActivity;
    private final Boolean inBreach;
}
