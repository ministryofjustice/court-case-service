package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

@Repository
public interface GroupedOffenderMatchRepository extends JpaRepository<GroupedOffenderMatchesEntity, Long> {
    List<GroupedOffenderMatchesEntity> findByCourtCase(CourtCaseEntity courtCase);
}
