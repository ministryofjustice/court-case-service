package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDateTime;

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
    public void whenUpdateCase_ThenUpdateAuditField() {

        LocalDateTime start = LocalDateTime.now();
        CourtCaseEntity courtCaseEntity = CourtCaseServiceTest.buildCourtCase();
        String courtCode = courtCaseEntity.getCourtCode();
        String caseNo = courtCaseEntity.getCaseNo();

        CourtCaseEntity createdCase = courtCaseService.createOrUpdateCase(courtCode, caseNo, courtCaseEntity);

        LocalDateTime created = createdCase.getCreated();
        LocalDateTime updated = createdCase.getLastUpdated();
        assertThat(created).isAfterOrEqualTo(start);
        assertThat(updated).isAfterOrEqualTo(start);
    }

    @Test
    public void whenDeleteExistingCase_ThenSoftDeleteAppliedIncludingChildOffences() {

        courtCaseService.delete(COURT_CODE, PRE_EXISTING_CASE_NO);

        CourtCaseEntity entity = courtCaseService.getCaseByCaseNumber(COURT_CODE, PRE_EXISTING_CASE_NO);
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getOffences()).hasSize(2);
        assertThat(entity.getOffences()).extracting("deleted").contains(true, true);
    }

}
