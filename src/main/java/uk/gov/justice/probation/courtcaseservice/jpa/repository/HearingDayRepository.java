package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;

@Repository
public interface HearingDayRepository extends CrudRepository<HearingDayEntity, Long>{


}
