package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

import static java.util.Objects.isNull;
@Repository
public class HearingSearchRepositoryCustomImpl implements HearingSearchRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {

        //select
        StringBuilder selectQueryBuilder = new StringBuilder();
        selectQueryBuilder.append("SELECT h.* from hearing h ");
        selectQueryBuilder.append("inner join hearing_day hd on hd.fk_hearing_id = h.id  ");
        if(isFilterBySourceType(hearingSearchFilter)){
            selectQueryBuilder.append("inner join court_case cc on cc.id = h.fk_court_case_id ");
        }

        //Where clause
        StringBuilder whereClauseBuilder = new StringBuilder("where ");
        whereClauseBuilder.append("hd.hearing_day = :hearingDay ");
        whereClauseBuilder.append("and hd.court_code = :courtCode ");
        if(isFilterBySourceType(hearingSearchFilter)){
            whereClauseBuilder.append("and cc.source_type = :sourceType ");
        }
        whereClauseBuilder.append("and h.deleted = false");

        //create a native query
        selectQueryBuilder.append(whereClauseBuilder);
        Query filterQuery = entityManager.createNativeQuery(selectQueryBuilder.toString(), HearingEntity.class);

        //apply filters
        filterQuery.setParameter("hearingDay", hearingSearchFilter.getHearingDay());
        filterQuery.setParameter("courtCode", hearingSearchFilter.getCourtCode());
        if(isFilterBySourceType(hearingSearchFilter)){
            filterQuery.setParameter("sourceType", SourceType.from(hearingSearchFilter.getSource().toUpperCase()).name());
        }

        return filterQuery.getResultList();
    }

    private boolean isFilterBySourceType(HearingSearchFilter hearingSearchFilter){
        return !isNull(hearingSearchFilter.getSource());
    }
}
