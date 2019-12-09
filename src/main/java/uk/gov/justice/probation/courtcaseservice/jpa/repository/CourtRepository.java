package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;

import java.util.Optional;

@Repository
public interface CourtRepository extends CrudRepository<CourtEntity, Long> {

}
