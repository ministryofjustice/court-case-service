package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefendantRepositoryCustomTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private Query countQuery;

    @InjectMocks
    private DefendantRepositoryCustom defendantRepositoryCustom;

    final static String testCrn = "X123456";
    final static Pageable pageable = Pageable.ofSize(10).withPage(0);
    final static String courtCode = "B01XX";
    final static String testTsQueryString = "";
    final static String testName = "John Smith";

    @Test
    void whenFindDefendantsByCrnGivenValidCourtCode_thenQueryBuiltCorrectly() {
        when(entityManager.createNativeQuery(anyString(), anyString()))
            .thenReturn(query);
        when(entityManager.createNativeQuery(startsWith("select count(*)")))
            .thenReturn(countQuery);

        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        defendantRepositoryCustom.findDefendantsByCrn(testCrn, pageable, courtCode);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(entityManager).createNativeQuery(
            sqlCaptor.capture(),
            eq("search_defendants_result_mapping")
        );

        String actualSql = sqlCaptor.getValue();

        assertTrue(actualSql.contains("hday1.court_code = :courtCode"));
    }

    @Test
    void whenFindDefendantsByCrnGivenBlankCourtCode_thenQueryBuiltCorrectly() {
        when(entityManager.createNativeQuery(anyString(), anyString()))
            .thenReturn(query);
        when(entityManager.createNativeQuery(startsWith("select count(*)")))
            .thenReturn(countQuery);

        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        defendantRepositoryCustom.findDefendantsByCrn(testCrn, pageable, "");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(entityManager).createNativeQuery(
            sqlCaptor.capture(),
            eq("search_defendants_result_mapping")
        );

        String actualSql = sqlCaptor.getValue();

        assertTrue(actualSql.contains("off.crn = :crn"));
        assertFalse(actualSql.contains("hday1.court_code = :courtCode"));
        assertTrue(actualSql.contains("order by cc.id desc"));
    }

    @Test
    void whenFindDefendantsByNameGivenValidCourtCode_thenQueryBuiltCorrectly() {
        when(entityManager.createNativeQuery(anyString(), anyString()))
            .thenReturn(query);
        when(entityManager.createNativeQuery(startsWith("select count(*)")))
            .thenReturn(countQuery);

        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        defendantRepositoryCustom.findDefendantsByName(testTsQueryString, testName, pageable, courtCode);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(entityManager).createNativeQuery(
            sqlCaptor.capture(),
            eq("search_defendants_result_mapping")
        );

        String actualSql = sqlCaptor.getValue();

        assertTrue(actualSql.contains("hday1.court_code = :courtCode"));
    }

    @Test
    void whenFindDefendantsByNameGivenBlankCourtCode_thenQueryBuiltCorrectly() {
        when(entityManager.createNativeQuery(anyString(), anyString()))
            .thenReturn(query);
        when(entityManager.createNativeQuery(startsWith("select count(*)")))
            .thenReturn(countQuery);

        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        defendantRepositoryCustom.findDefendantsByName(testTsQueryString, testName, pageable, "");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(entityManager).createNativeQuery(
            sqlCaptor.capture(),
            eq("search_defendants_result_mapping")
        );

        String actualSql = sqlCaptor.getValue();

        assertTrue(actualSql.contains("where d1.tsv_name @@ to_tsquery('simple', :tsQueryString)"));
        assertFalse(actualSql.contains("hday1.court_code = :courtCode"));
        assertTrue(actualSql.contains("order by similarity (d.defendant_name, :name) desc "));
    }
}
