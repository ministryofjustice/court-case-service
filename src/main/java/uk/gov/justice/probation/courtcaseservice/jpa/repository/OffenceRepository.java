package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

public interface OffenceRepository extends CrudRepository<OffenceEntity, Long> {
}
