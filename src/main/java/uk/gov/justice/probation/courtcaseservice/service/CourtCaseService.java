package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

public interface CourtCaseService {
    HearingEntity getHearingByCaseNumber(String courtCode, String caseNo, String listNo) throws EntityNotFoundException;

    HearingEntity getHearingByHearingId(String hearingId) throws EntityNotFoundException;

    HearingEntity getHearingByHearingIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException;

    Mono<HearingEntity> createHearing(String caseId, HearingEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    Mono<HearingEntity> createOrUpdateHearingByHearingId(String hearingId, HearingEntity updatedHearing) throws EntityNotFoundException, InputMismatchException;

    List<HearingEntity> filterHearings(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore);

    Optional<LocalDateTime> filterHearingsLastModified(String courtCode, LocalDate date);

    Page<HearingEntity> searchCourtCases(CaseSearchFilter caseSearchFilter, Pageable pageable);
}
