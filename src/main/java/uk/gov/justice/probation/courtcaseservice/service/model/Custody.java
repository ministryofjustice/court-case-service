package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class Custody {
    private final LocalDate homeDetentionCurfewDateActual;
    private final LocalDate homeDetentionCurfewEndDate;
    private final LocalDate licenceExpiryDate;
    private final LocalDate releaseDate;
    private final LocalDate topupSupervisionStartDate;
    private final LocalDate topupSupervisionExpiryDate;
}
