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

    private static final String SELECT_QUERY = "SELECT h.* from hearing h ";
    private static final String HEARING_HEARING_DAY_JOIN = "inner join hearing_day hd on hd.fk_hearing_id = h.id  ";
    private static final String COURT_CASE_HEARING_JOIN = "inner join court_case cc on cc.id = h.fk_court_case_id ";
    private static final String HEARING_DEFENDANT_HEARING_JOIN = "inner join hearing_defendant hdef on hdef.fk_hearing_id = h.id  ";
    private static final String HEARING_DEFENDANT_DEFENDANT_JOIN = "inner join defendant d on d.id = hdef.fk_defendant_id  ";
    public static final String DEFENDANT_OFFENDER_JOIN = "inner join offender o on o.id = d.fk_offender_id  ";

    public static final String HEARING_DAY = "hd.hearing_day = :hearingDay ";
    public static final String COURT_CODE = "and hd.court_code = :courtCode ";
    public static final String SOURCE_TYPE = "and cc.source_type = :sourceType ";
    public static final String BREACH = "and o.breach = true ";
    public static final String DELETED = "and h.deleted = false";

    public static final String HEARING_DAY_NAMED_PARAM = "hearingDay";
    public static final String COURT_CODE_NAMED_PARAM = "courtCode";
    public static final String SOURCE_TYPE_NAMED_PARAM = "sourceType";


    @Override
    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {

        //select
        StringBuilder selectQueryBuilder = new StringBuilder();
        selectQueryBuilder.append(SELECT_QUERY);

        //joins
        StringBuilder joinQueryBuilder = new StringBuilder();
        joinQueryBuilder.append(HEARING_HEARING_DAY_JOIN);
        if (isFilterBySourceType(hearingSearchFilter)) {
            joinQueryBuilder.append(COURT_CASE_HEARING_JOIN);
        }
        if (isFilterByBreach(hearingSearchFilter)) {
            joinQueryBuilder.append(getOffenderJoin());
        }

        //Where clause
        StringBuilder whereClauseBuilder = new StringBuilder("where ");
        whereClauseBuilder.append(HEARING_DAY);
        whereClauseBuilder.append(COURT_CODE);
        if (isFilterBySourceType(hearingSearchFilter)) {
            whereClauseBuilder.append(SOURCE_TYPE);
        }
        if (isFilterByBreach(hearingSearchFilter)) {
            whereClauseBuilder.append(BREACH);
        }
        whereClauseBuilder.append(DELETED);

        //combine query builders
        selectQueryBuilder.append(joinQueryBuilder).append(whereClauseBuilder);

        //create a native query
        Query filterNativeQuery = entityManager.createNativeQuery(selectQueryBuilder.toString(), HearingEntity.class);

        //set parameters
        filterNativeQuery.setParameter(HEARING_DAY_NAMED_PARAM, hearingSearchFilter.getHearingDay());
        filterNativeQuery.setParameter(COURT_CODE_NAMED_PARAM, hearingSearchFilter.getCourtCode());
        if (isFilterBySourceType(hearingSearchFilter)) {
            filterNativeQuery.setParameter(SOURCE_TYPE_NAMED_PARAM, SourceType.from(hearingSearchFilter.getSource().toUpperCase()).name());
        }
        return filterNativeQuery.getResultList();
    }

    private StringBuilder getOffenderJoin() {
        StringBuilder offenderJoin = new StringBuilder();
        offenderJoin.append(HEARING_DEFENDANT_HEARING_JOIN);
        offenderJoin.append(HEARING_DEFENDANT_DEFENDANT_JOIN);
        offenderJoin.append(DEFENDANT_OFFENDER_JOIN);
        return offenderJoin;
    }

    private boolean isFilterBySourceType(HearingSearchFilter hearingSearchFilter) {
        return !isNull(hearingSearchFilter.getSource());
    }

    private boolean isFilterByBreach(HearingSearchFilter hearingSearchFilter) {
        return hearingSearchFilter.isBreach();
    }
}
