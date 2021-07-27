package uk.gov.justice.probation.courtcaseservice.restclient.prisonapi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class PrisonApiSentenceDetail {
    private final LocalDate homeDetentionCurfewActualDate;
    private final LocalDate homeDetentionCurfewEndDate;
    private final LocalDate licenceExpiryDate;
    private final LocalDate releaseDate;
    private final LocalDate topupSupervisionStartDate;
    private final LocalDate topupSupervisionExpiryDate;
}
