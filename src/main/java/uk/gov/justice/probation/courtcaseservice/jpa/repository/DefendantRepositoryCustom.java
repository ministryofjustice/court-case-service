package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefendantRepositoryCustom {
    private static final String DEFENDANT_SEARCH_FROM_CLAUSE = " from court_case cc " +
        "        join hearing h on cc.id = h.fk_court_case_id " +
        "        join hearing_defendant hd on h.id = hd.fk_hearing_id " +
        "        join defendant d on d.id = hd.fk_defendant_id " +
        "  inner join (select max(hearing.id) as max_id, defendant.id as did from hearing " +
        "         join court_case on court_case.id = hearing.fk_court_case_id " +
        "         join hearing_defendant on hearing.id = hearing_defendant.fk_hearing_id " +
        "         join defendant on defendant.id = hearing_defendant.fk_defendant_id ";

    private static final String DEFENDANT_SEARCH_SELECT = "select cc.*, d.* ";

    private static String DEFENDANT_SEARCH_GROUPING = " group by defendant.id, court_case.id) grouped_cases on h.id = grouped_cases.max_id and d.id = grouped_cases.did ";

    @PersistenceContext
    private EntityManager entityManager;
    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByCrn(String crn, Pageable pageable) {

        String CRN_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " where defendant.crn = :crn " + DEFENDANT_SEARCH_GROUPING;

        var query = entityManager.createNativeQuery(
            DEFENDANT_SEARCH_SELECT + CRN_SEARCH_FROM + " order by cc.id desc ",
            "search_defendants_result_mapping");

        query.setParameter("crn", crn);

        var countQuery = entityManager.createNativeQuery("select count(*) " + CRN_SEARCH_FROM);

        countQuery.setParameter("crn", crn);

        return getPagedResult(pageable, query, countQuery);
    }

    public Page<Pair<CourtCaseEntity, DefendantEntity>> findDefendantsByName(String tsQueryString, String name, Pageable pageable) {

        String NAME_SEARCH_FROM = DEFENDANT_SEARCH_FROM_CLAUSE + " where defendant.tsv_name @@ to_tsquery(:tsQueryString) " + DEFENDANT_SEARCH_GROUPING;

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

        int count = ((BigInteger) countQuery.getSingleResult()).intValue();

        return new PageImpl<>(courtCases, pageable, count);
    }
}
