package uk.gov.justice.probation.courtcaseservice.service;

import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

public interface CourtCaseService {
    CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException;

    CourtCaseEntity getCaseByCaseId(String caseId) throws EntityNotFoundException;

    CourtCaseEntity getCaseByCaseIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException;

    Mono<CourtCaseEntity> createCase(String caseId, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    Mono<CourtCaseEntity> createUpdateCaseForSingleDefendantId(String caseId, String defendantId, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    List<CourtCaseEntity> filterCases(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore);

    Optional<LocalDateTime> filterCasesLastModified(String courtCode, LocalDate date);

}
