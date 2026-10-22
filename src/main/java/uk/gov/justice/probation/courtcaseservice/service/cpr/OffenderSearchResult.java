package uk.gov.justice.probation.courtcaseservice.service.cpr;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;

import java.time.LocalDate;

public record OffenderSearchResult(
        String crn,
        String pnc,
        String cro,
        OffenderProbationStatus probationStatus,
        LocalDate previouslyKnownTerminationDate,
        Boolean breach,
        boolean preSentenceActivity,
        Boolean awaitingPsr
) {
}