package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefendantRepositoryCustom {
    private static final String DEFENDANT_SEARCH_FROM_CLAUSE = " from court_case cc " +
        "        join hearing h on cc.id = h.fk_court_case_id " +
        "        join hearing_defendant hd on h.id = hd.fk_hearing_id " +
        "        join defendant d on d.id = hd.fk_defendant_id " +
        "  inner join (select max(h1.id) as max_id, d1.id as did from hearing h1 " +
        "         join court_case cc1 on cc1.id = h1.fk_court_case_id " +
        "         join hearing_defendant hd1  on h1.id = hd1.fk_hearing_id " +
        "         join defendant d1 on d1.id = hd1.fk_defendant_id ";

    private static final String DEFENDANT_SEARCH_SELECT =
            "select cc.id, cc.case_id, cc.case_no, cc.created AS ccCreated, cc.created_by AS ccCreatedBy, cc.deleted AS ccDeleted, cc.source_type, cc.urn, cc.last_updated AS ccLastUpdated, cc.last_updated_by AS ccLastUpdatedBy, cc.\"version\" AS ccVersion, " +
                  "d.id as defId, d.defendant_name, d.\"type\", d.\"name\", d.address, d.crn, d.pnc, d.cro, d.date_of_birth, d.sex, d.nationality_1, d.nationality_2, d.created, " +
                  "d.created_by, d.manual_update, d.defendant_id, d.offender_confirmed, d.phone_number, d.person_id, d.fk_offender_id, d.last_updated, d.last_updated_by, d.\"version\", d.deleted, d.tsv_name, d.cpr_uuid, d.c_id ";

    private static String DEFENDANT_SEARCH_GROUPING = " group by d1.id, cc1.id) grouped_cases on h.id = grouped_cases.max_id and d.id = grouped_cases.did ";

    @PersistenceContext
    private EntityManager entityManager;
    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByCrn(String crn, Pageable pageable) {

        String CRN_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " join offender off on off.id = d1.fk_offender_id " + " where off.crn = :crn " + DEFENDANT_SEARCH_GROUPING;

        var query = entityManager.createNativeQuery(
            DEFENDANT_SEARCH_SELECT + CRN_SEARCH_FROM + " order by cc.id desc ",
            "search_defendants_result_mapping");

        query.setParameter("crn", crn);

        var countQuery = entityManager.createNativeQuery("select count(*) " + CRN_SEARCH_FROM);

        countQuery.setParameter("crn", crn);

        return getPagedResult(pageable, query, countQuery);
    }

    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByName(String tsQueryString, String name, Pageable pageable) {

        String NAME_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " where d1.tsv_name @@ to_tsquery('simple', :tsQueryString) " + DEFENDANT_SEARCH_GROUPING;

        String NAME_SEARCH_QUERY = DEFENDANT_SEARCH_SELECT + NAME_SEARCH_FROM + " order by similarity (d.defendant_name, :name) desc ";

        var query = entityManager.createNativeQuery(NAME_SEARCH_QUERY, "search_defendants_result_mapping");

        query.setParameter("tsQueryString", tsQueryString);
        query.setParameter("name", name);

        var countQuery = entityManager.createNativeQuery("select count(*) " + NAME_SEARCH_FROM );

        countQuery.setParameter("tsQueryString", tsQueryString);

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
}
