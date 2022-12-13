package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = { "classpath:sql/before-common.sql", "classpath:sql/before-search-tests.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CaseSearchServiceIntTest extends BaseIntTest {

    @Autowired
    CaseSearchService defendantSearchService;

    @Test
    void givenValidCrn_shouldReturnSearchResponse() {
        var actual = defendantSearchService.searchByCrn("X25829");

        var result1 = CaseSearchResultItem.builder()
            .defendantName("Mr Ferris BUELLER")
            .crn("X25829")
            .offenceTitles(List.of("Theft from a garage"))
            .lastHearingDate(LocalDate.now().minusDays(5))
            .lastHearingCourt("Leicester")
            .probationStatus(DefendantProbationStatus.CURRENT)
            .build();
        var result2 = CaseSearchResultItem.builder()
            .defendantName("Mr Ferris Biller")
            .crn("X25829")
            .offenceTitles(List.of("Theft from a hospital", "Theft from a shop"))
            .lastHearingDate(LocalDate.now().minusDays(2))
            .lastHearingCourt("Sheffield")
            .nextHearingDate(LocalDate.now().plusDays(10))
            .nextHearingCourt("Leicester")
            .probationStatus(DefendantProbationStatus.CURRENT)
            .build();

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of(result1, result2)).build());
    }
}