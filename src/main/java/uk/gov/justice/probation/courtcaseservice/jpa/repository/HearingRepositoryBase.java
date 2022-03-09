package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HearingRepositoryBase {
    Optional<HearingEntity> findFirstByHearingIdOrderByIdDesc(String hearingId);

    Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    Optional<HearingEntity> findByCaseId(String caseId);

    Optional<HearingEntity> findByHearingIdAndDefendantId(String hearingId, String defendantId);

    List<HearingEntity> findByCourtCodeAndHearingDay(
            String courtCode,
            LocalDate hearingDay,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    );

    Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay);
}
