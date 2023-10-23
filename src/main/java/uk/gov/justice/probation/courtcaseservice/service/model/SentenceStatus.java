package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;

import java.time.LocalDate;


@Builder
public record SentenceStatus(Long sentenceId, String sentenceDescription, KeyValue custodialType,
                             String mainOffenceDescription, LocalDate sentenceDate, LocalDate actualReleaseDate,
                             LocalDate licenceExpiryDate, LocalDate pssEndDate, Integer length, String lengthUnits) {
}



