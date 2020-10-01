package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseServiceIntTest extends BaseIntTest {

    private static final String COURT_CODE = "SHF";
    private static final String PRE_EXISTING_CASE_NO = "1600028913";

    @Autowired
    private CourtCaseService courtCaseService;

    @Test
    @Ignore
    @Disabled("To be deleted")
    public void whenUpdateCase_ThenUpdateAuditField() {

        LocalDateTime start = LocalDateTime.now();
        CourtCaseEntity courtCaseEntity = CourtCaseServiceTest.buildCourtCase(CourtCaseServiceTest.CRN);
        String courtCode = courtCaseEntity.getCourtCode();
        String caseNo = courtCaseEntity.getCaseNo();

        CourtCaseEntity createdCase = courtCaseService.createOrUpdateCase(courtCode, caseNo, courtCaseEntity);

        LocalDateTime created = createdCase.getCreated();
        LocalDateTime updated = createdCase.getLastUpdated();
        assertThat(created).isAfterOrEqualTo(start);
        assertThat(updated).isAfterOrEqualTo(start);
    }

    @Test
    @Ignore
    @Disabled("To be deleted")
    public void whenDeleteExistingCase_ThenSoftDeleteAppliedIncludingChildOffences() {

        courtCaseService.delete(COURT_CODE, PRE_EXISTING_CASE_NO);

        CourtCaseEntity entity = courtCaseService.getCaseByCaseNumber(COURT_CODE, PRE_EXISTING_CASE_NO);
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getOffences()).hasSize(2);
        assertThat(entity.getOffences()).extracting("deleted").contains(true, true);
    }

    @Test
    public void whenDeleteMissingCases_ThenSoftDeleteAppliedIncludingChildOffences() {

        // 2nd Jan has 1000002 through 1000007 to start with so here 1000003 and 1000007 should remain, others soft deleted
        LocalDate date2Jan = LocalDate.of(2020, Month.JANUARY, 2);
        List<String> existingCases = Arrays.asList("1000003", "1000007");
        final Map<LocalDate, List<String>> existing = Map.of(date2Jan, existingCases);

        courtCaseService.deleteAbsentCases(COURT_CODE, existing);

        List<String> expectedDeletions = Arrays.asList("1000002", "1000004", "1000005", "1000006");
        expectedDeletions.forEach(caseNo -> {
            CourtCaseEntity entity = courtCaseService.getCaseByCaseNumber(COURT_CODE, caseNo);
            assertThat(entity.isDeleted()).isTrue();
            if (!entity.getOffences().isEmpty()) {
                assertThat(entity.getOffences()).extracting("deleted").containsOnly(true);
            }
        });

        existingCases.forEach(caseNo -> {
            CourtCaseEntity entity = courtCaseService.getCaseByCaseNumber(COURT_CODE, caseNo);
            assertThat(entity.isDeleted()).isFalse();
            if (!entity.getOffences().isEmpty()) {
                assertThat(entity.getOffences()).extracting("deleted").containsOnly(false);
            }
        });
    }

}
