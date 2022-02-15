package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long> {

}
