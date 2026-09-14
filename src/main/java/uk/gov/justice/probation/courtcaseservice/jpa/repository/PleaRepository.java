package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity;

@Repository
public interface PleaRepository extends CrudRepository<PleaEntity, Long> {}
