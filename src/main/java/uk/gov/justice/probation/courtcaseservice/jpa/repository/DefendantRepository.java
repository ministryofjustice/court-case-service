package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import java.util.Optional;

public interface DefendantRepository extends CrudRepository<DefendantEntity, Long> {
    Optional<DefendantEntity> findFirstByDefendantId(String defendantId);
}
