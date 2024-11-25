package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingCourtCaseEntity;

@Repository
public interface HearingCourtCaseRepository extends JpaRepository<HearingCourtCaseEntity, Long> {
}
