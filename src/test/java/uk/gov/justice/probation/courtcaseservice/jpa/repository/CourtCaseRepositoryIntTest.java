package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = {
        "classpath:sql/before-common.sql",
        "classpath:sql/before-CourtCaseRepositoryTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
class CourtCaseRepositoryIntTest extends BaseRepositoryIntTest {
    @Autowired
    private CourtCaseRepository courtCaseRepository;

    @Test
    public void findByCaseId_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = courtCaseRepository.findByCaseId("created_clash_id_1");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findByCourtCodeAndCaseNo_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = courtCaseRepository.findByCourtCodeAndCaseNo("B10JQ", "1600028913");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findByCaseIdAndDefendantId_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = courtCaseRepository.findByCaseIdAndDefendantId("created_clash_id_1", "40db17d6-04db-11ec-b2d8-0242ac130002");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findLastModifiedByHearingDay_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var lastModified = courtCaseRepository.findLastModifiedByHearingDay("B10JQ", LocalDate.of(2019, 12, 14));
        assertThat(lastModified).isPresent();
        assertThat(lastModified.get()).isEqualTo(LocalDateTime.of(2020, 9, 1, 16, 59, 59, 0));
    }
}
