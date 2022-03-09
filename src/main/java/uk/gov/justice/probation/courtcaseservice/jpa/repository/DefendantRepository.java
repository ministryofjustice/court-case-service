package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

public interface DefendantRepository extends CrudRepository<DefendantEntity, Long> {
    DefendantEntity findByDefendantId(String defendantId);
}
