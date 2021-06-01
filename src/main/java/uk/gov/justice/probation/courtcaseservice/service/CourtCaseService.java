package uk.gov.justice.probation.courtcaseservice.service;

import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;

public interface CourtCaseService {
    CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException;

    Mono<CourtCaseEntity> createCase(String courtCode, String caseNo, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    List<CourtCaseEntity> filterCases(String courtCode, LocalDate date, LocalDateTime createdAfter, LocalDateTime createdBefore);

    LocalDateTime filterCasesLastModified(String courtCode, LocalDate date);
}
