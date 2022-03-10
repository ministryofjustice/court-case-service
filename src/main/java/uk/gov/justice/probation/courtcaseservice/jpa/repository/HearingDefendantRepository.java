package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;

public interface HearingDefendantRepository extends CrudRepository<HearingDefendantEntity, Long> {
}
