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
        "classpath:sql/before-RepositoryTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
class HearingRepositoryIntTest extends BaseRepositoryIntTest {
    @Autowired
    private HearingRepository hearingRepository;

    @Test
    public void findByCaseId_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = hearingRepository.findByCaseId("created_clash_id_1");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findByCourtCodeAndCaseNo_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = hearingRepository.findByCourtCodeAndCaseNo("B10JQ", "1600028913");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findByCaseIdAndDefendantId_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var courtCase = hearingRepository.findByCaseIdAndDefendantId("created_clash_id_1", "40db17d6-04db-11ec-b2d8-0242ac130002");
        assertThat(courtCase).isPresent();
        assertThat(courtCase.get().getId()).isEqualTo(-1700028900L);
    }

    @Test
    public void findLastModifiedByHearingDay_shouldReturnOneResultWhereCreatedTimestampsClash() {
        final var lastModified = hearingRepository.findLastModifiedByHearingDay("B10JQ", LocalDate.of(2019, 12, 14));
        assertThat(lastModified).isPresent();
        assertThat(lastModified.get()).isEqualTo(LocalDateTime.of(2020, 9, 1, 16, 59, 59, 0));
    }

    @Test
    public void findByCourtCodeAndHearingDay_shouldReturnExpectedCases() {
        final var hearings = hearingRepository.findByCourtCodeAndHearingDay("B10JQ", LocalDate.of(2022, 2, 17), LocalDateTime.of(2000, 1, 1, 1, 1), LocalDateTime.of(2500, 1, 1, 1, 1));
        assertThat(hearings).asList()
                .extracting("id")
                .containsExactlyInAnyOrder(-1700028904L, -1700028953L, -1700028952L,
                        -1700028907L); // <- This shouldn't be included, see PIC-1958
    }
}
