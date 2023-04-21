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
        if(isFilterByBreach(hearingSearchFilter)){
            selectQueryBuilder.append(getOffenderJoin());
        }

        //Where clause
        StringBuilder whereClauseBuilder = new StringBuilder("where ");
        whereClauseBuilder.append("hd.hearing_day = :hearingDay ");
        whereClauseBuilder.append("and hd.court_code = :courtCode ");
        if(isFilterBySourceType(hearingSearchFilter)){
            whereClauseBuilder.append("and cc.source_type = :sourceType ");
        }
        if(isFilterByBreach(hearingSearchFilter)){
            whereClauseBuilder.append("and o.breach = true ");
        }
        whereClauseBuilder.append("and h.deleted = false");

        //create a native query
        selectQueryBuilder.append(whereClauseBuilder);
        Query filterQuery = entityManager.createNativeQuery(selectQueryBuilder.toString(), HearingEntity.class);

        //set parameters
        filterQuery.setParameter("hearingDay", hearingSearchFilter.getHearingDay());
        filterQuery.setParameter("courtCode", hearingSearchFilter.getCourtCode());
        if(isFilterBySourceType(hearingSearchFilter)){
            filterQuery.setParameter("sourceType", SourceType.from(hearingSearchFilter.getSource().toUpperCase()).name());
        }

        return filterQuery.getResultList();
    }

    private StringBuilder getOffenderJoin(){
        StringBuilder offenderJoin = new StringBuilder();
        offenderJoin.append("inner join hearing_defendant hdef on hdef.fk_hearing_id = h.id  ");
        offenderJoin.append("inner join defendant d on d.id = hdef.fk_defendant_id  ");
        offenderJoin.append("inner join offender o on o.id = d.fk_offender_id  ");
        return offenderJoin;
    }

    private boolean isFilterBySourceType(HearingSearchFilter hearingSearchFilter){
        return !isNull(hearingSearchFilter.getSource());
    }

    private boolean isFilterByBreach(HearingSearchFilter hearingSearchFilter){
        return hearingSearchFilter.isBreach();
    }
}
