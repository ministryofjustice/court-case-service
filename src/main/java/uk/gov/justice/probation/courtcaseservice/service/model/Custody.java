package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Data
public class Custody {
    private final LocalDate homeDetentionCurfewActualDate;
    private final LocalDate homeDetentionCurfewEndDate;
    private final LocalDate licenceExpiryDate;
    private final LocalDate releaseDate;
    private final LocalDate topupSupervisionStartDate;
    private final LocalDate topupSupervisionExpiryDate;
}
