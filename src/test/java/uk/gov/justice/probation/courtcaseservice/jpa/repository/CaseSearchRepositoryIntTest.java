package uk.gov.justice.probation.courtcaseservice.jpa.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.specification.CaseSearchSpecification;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = {
        "classpath:sql/before-common.sql",
        "classpath:sql/before-RepositoryTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class CaseSearchRepositoryIntTest extends BaseRepositoryIntTest {

    @Autowired
    private CaseSearchRepository caseSearchRepository;

    @Test
    public void findAll_shouldReturnExpectedCases() {
        CaseSearchFilter searchFilter = CaseSearchFilter.builder()
                .courtCode("B10JQ")
                .date(LocalDate.of(2022, 2, 17))
                .build();
        Specification<HearingEntity> searchSpecification = CaseSearchSpecification.getCaseSearchSpecification(searchFilter);

        final var hearings = caseSearchRepository.findAll(searchSpecification);

        assertThat(hearings).asList()
                .extracting("id")
                .containsExactlyInAnyOrder(-1700028904L, -1700028953L, -1700028952L);
    }
}
