package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class HearingFilterRepositoryCustomImpl implements HearingFilterRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {
        //TODO build custom native query
        return null;
    }
}
