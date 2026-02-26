package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

@Repository
public interface OffenceRepository extends CrudRepository<OffenceEntity, Long> {}
