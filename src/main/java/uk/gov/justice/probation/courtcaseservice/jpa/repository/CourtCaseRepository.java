package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourtCaseRepository extends JpaRepository<CourtCaseEntity, Long> {
    Optional<CourtCaseEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    List<CourtCaseEntity> findByCourtCodeAndSessionStartTimeBetween(String courtCode, LocalDateTime start, LocalDateTime end);

    Optional<CourtCaseEntity> findByCaseId(String caseId);
}
