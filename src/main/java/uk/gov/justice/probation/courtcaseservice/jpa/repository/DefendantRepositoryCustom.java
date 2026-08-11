package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchSortFields;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefendantRepositoryCustom {
    private static final String DEFAULT_CASE_ORDER_BY = " order by cc.id desc ";
    private static final String DEFAULT_NAME_ORDER_BY = " order by similarity (d.defendant_name, :name) desc ";
    private static final String NEXT_HEARING_DATE_ORDER_BY = """
         order by (
             select min(hday2.hearing_day)
             from hearing h2
             join hearing_defendant hd2 on h2.id = hd2.fk_hearing_id
             join hearing_day hday2 on hday2.fk_hearing_id = h2.id
             where h2.fk_court_case_id = cc.id
               and hd2.fk_defendant_id = d.id
               and (hday2.hearing_day > current_date or (hday2.hearing_day = current_date and hday2.hearing_time > localtime))
         ) %s nulls last, cc.id %s
        """;
    private static final String DEFENDANT_SEARCH_FROM_CLAUSE = " from court_case cc " +
        "        join hearing h on cc.id = h.fk_court_case_id " +
        "        join hearing_defendant hd on h.id = hd.fk_hearing_id " +
        "        join defendant d on d.id = hd.fk_defendant_id " +
        "  inner join (select max(h1.id) as max_id, d1.id as did from hearing h1 " +
        "         join court_case cc1 on cc1.id = h1.fk_court_case_id " +
        "         join hearing_defendant hd1  on h1.id = hd1.fk_hearing_id " +
        "         join defendant d1 on d1.id = hd1.fk_defendant_id " +
        "         inner join hearing_day hday1 on hday1.fk_hearing_id = h1.id";

    private static final String DEFENDANT_SEARCH_SELECT =
            "select cc.id, cc.case_id, cc.case_no, cc.created AS ccCreated, cc.created_by AS ccCreatedBy, cc.deleted AS ccDeleted, cc.source_type, cc.urn, cc.last_updated AS ccLastUpdated, cc.last_updated_by AS ccLastUpdatedBy, cc.\"version\" AS ccVersion, " +
                  "d.id as defId, d.defendant_name, d.\"type\", d.\"name\", d.address, d.crn, d.pnc, d.cro, d.date_of_birth, d.sex, d.nationality_1, d.nationality_2, d.created, " +
                  "d.created_by, d.manual_update, d.defendant_id, d.offender_confirmed, d.phone_number, d.person_id, d.fk_offender_id, d.last_updated, d.last_updated_by, d.\"version\", d.deleted, d.tsv_name, d.cpr_uuid, d.c_id ";

    private static String DEFENDANT_SEARCH_GROUPING = " group by d1.id, cc1.id) grouped_cases on h.id = grouped_cases.max_id and d.id = grouped_cases.did ";

    @PersistenceContext
    private EntityManager entityManager;
    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByCrn(String crn, Pageable pageable, String courtCode,
                                                                            CaseSearchSortFields sortBy, Direction order) {

        String COURT_FILTER_FROM = (!courtCode.isBlank()) ? " and hday1.court_code = :courtCode" : "";

        String CRN_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " join offender off on off.id = d1.fk_offender_id " + " where off.crn = :crn " + COURT_FILTER_FROM + DEFENDANT_SEARCH_GROUPING;

        var query = entityManager.createNativeQuery(
            DEFENDANT_SEARCH_SELECT + CRN_SEARCH_FROM + getOrderByClause(sortBy, order, DEFAULT_CASE_ORDER_BY),
            "search_defendants_result_mapping");

        query.setParameter("crn", crn);

        var countQuery = entityManager.createNativeQuery("select count(*) " + CRN_SEARCH_FROM);

        if (!courtCode.isBlank()) {
            query.setParameter("courtCode", courtCode);
            countQuery.setParameter("courtCode", courtCode);
        }
        
        countQuery.setParameter("crn", crn);

        return getPagedResult(pageable, query, countQuery);
    }

    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByName(String tsQueryString, String name, Pageable pageable, String courtCode,
                                                                             CaseSearchSortFields sortBy, Direction order) {

        String COURT_FILTER_FROM = (!courtCode.isBlank()) ? " and hday1.court_code = :courtCode" : "";

        String NAME_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " where d1.tsv_name @@ to_tsquery('simple', :tsQueryString) " + COURT_FILTER_FROM + DEFENDANT_SEARCH_GROUPING;

        String NAME_SEARCH_QUERY = DEFENDANT_SEARCH_SELECT + NAME_SEARCH_FROM + getOrderByClause(sortBy, order, DEFAULT_NAME_ORDER_BY);

        var query = entityManager.createNativeQuery(NAME_SEARCH_QUERY, "search_defendants_result_mapping");

        query.setParameter("tsQueryString", tsQueryString);
        setParameterIfPresent(query, NAME_SEARCH_QUERY, "name", name);

        var countQuery = entityManager.createNativeQuery("select count(*) " + NAME_SEARCH_FROM );

        if (!courtCode.isBlank()) {
            query.setParameter("courtCode", courtCode);
            countQuery.setParameter("courtCode", courtCode);
        }

        countQuery.setParameter("tsQueryString", tsQueryString);

        return getPagedResult(pageable, query, countQuery);
    }

    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByUrn(String urn, Pageable pageable, String courtCode,
                                                                            CaseSearchSortFields sortBy, Direction order) {

        String COURT_FILTER_FROM = (!courtCode.isBlank()) ? " and hday1.court_code = :courtCode" : "";

        String URN_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " where cc1.urn = :urn " + COURT_FILTER_FROM  + DEFENDANT_SEARCH_GROUPING;

        var query = entityManager.createNativeQuery(
            DEFENDANT_SEARCH_SELECT + URN_SEARCH_FROM + getOrderByClause(sortBy, order, DEFAULT_CASE_ORDER_BY),
            "search_defendants_result_mapping");

        query.setParameter("urn", urn);

        var countQuery = entityManager.createNativeQuery("select count(*) " + URN_SEARCH_FROM);

        if (!courtCode.isBlank()) {
            query.setParameter("courtCode", courtCode);
            countQuery.setParameter("courtCode", courtCode);
        }

        countQuery.setParameter("urn", urn);

        return getPagedResult(pageable, query, countQuery);
    }

    @NotNull
    private static PageImpl<Pair<CourtCaseEntity, DefendantEntity>> getPagedResult(Pageable pageable, Query query, Query countQuery) {

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> result = query.getResultList();
        var courtCases = result.stream()
            .map(objects -> new Pair<>((CourtCaseEntity)objects[0], (DefendantEntity)objects[1])).collect(Collectors.toList());

        int count = ((Long) countQuery.getSingleResult()).intValue();

        return new PageImpl<>(courtCases, pageable, count);
    }

    private static String getOrderByClause(CaseSearchSortFields sortBy, Direction order, String defaultOrderBy) {
        if (sortBy == null) {
            return defaultOrderBy;
        }

        var direction = order == null ? Direction.ASC.name() : order.name();

        return switch (sortBy) {
            case NEXT_HEARING_DATE -> NEXT_HEARING_DATE_ORDER_BY.formatted(direction, direction);
        };
    }

    private static void setParameterIfPresent(Query query, String sql, String parameterName, Object value) {
        if (sql.contains(":" + parameterName)) {
            query.setParameter(parameterName, value);
        }
    }
}
