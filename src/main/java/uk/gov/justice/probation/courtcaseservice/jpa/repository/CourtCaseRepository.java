package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

@Repository
public interface CourtCaseRepository extends JpaRepository<CourtCaseEntity, Long> {
    CourtCaseEntity findByCaseNo(String caseNo);
    CourtCaseEntity findByCaseId(Long caseId);
}
