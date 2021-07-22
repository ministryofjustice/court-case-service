package uk.gov.justice.probation.courtcaseservice.restclient.prisonapi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class PrisonApiSentenceDetail {
    private final ZonedDateTime homeDetentionCurfewActualDate;
    private final ZonedDateTime homeDetentionCurfewEndDate;
    private final ZonedDateTime licenceExpiryDate;
    private final ZonedDateTime releaseDate;
    private final ZonedDateTime topupSupervisionStartDate;
    private final ZonedDateTime topupSupervisionExpiryDate;
}
