package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

@Repository
public interface GroupedOffenderMatchRepository extends JpaRepository<GroupedOffenderMatchesEntity, Long> {
}
