package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity;

@Repository
public interface JudicialResultRepository extends CrudRepository<JudicialResultEntity, Long> {}
