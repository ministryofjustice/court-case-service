package uk.gov.justice.probation.courtcaseservice.service;

import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

public interface CourtCaseService {
    HearingEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException;

    HearingEntity getCaseByCaseId(String caseId) throws EntityNotFoundException;

    HearingEntity getCaseByCaseIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException;

    Mono<HearingEntity> createCase(String caseId, HearingEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    Mono<HearingEntity> createUpdateCaseForSingleDefendantId(String caseId, String defendantId, HearingEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    List<HearingEntity> filterCases(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore);

    Optional<LocalDateTime> filterCasesLastModified(String courtCode, LocalDate date);

}
