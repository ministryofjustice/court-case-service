package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

@Repository
public interface OffenderRepository extends CrudRepository<OffenderEntity, Long> {
    Optional<OffenderEntity> findByCrn(String crn);
}
