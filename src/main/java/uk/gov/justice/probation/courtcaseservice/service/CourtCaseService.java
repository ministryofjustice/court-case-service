package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

public interface CourtCaseService {
    HearingEntity getHearingByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException;

    HearingEntity getHearingByCaseId(String caseId) throws EntityNotFoundException;

    HearingEntity getHearingByCaseIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException;

    Mono<HearingEntity> createHearing(String caseId, HearingEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    Mono<HearingEntity> createUpdateHearingForSingleDefendantId(String caseId, String defendantId, HearingEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    HearingEntity getHearingByHearingIdAndDefendantId(String hearingId, String defendantId) throws EntityNotFoundException;

    List<HearingEntity> filterHearings(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore);

    Optional<LocalDateTime> filterHearingsLastModified(String courtCode, LocalDate date);

}
