package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HearingCourtCaseRepository extends JpaRepository<HearingCourtCaseEntity, Long> {
    HearingCourtCaseEntity findHearingCourtCaseEntityByHearingIdAndCaseId(String hearingId, String caseId);
}
