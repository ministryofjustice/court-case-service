package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity;

@Repository
public interface VerdictRepository extends CrudRepository<VerdictEntity, Long> {}
