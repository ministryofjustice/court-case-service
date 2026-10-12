package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-new-hearing-search.sql"],
  config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class DefendantEnrichmentRepositoryIntTest {
  private val fromDate = LocalDate.of(2026, 10, 9)
  private val toDate = LocalDate.of(2026, 10, 23)
  private val pageable = PageRequest.of(0, 200)

  @Autowired
  lateinit var repository: DefendantEnrichmentRepository

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun setUpTargets() {
    jdbcTemplate.update(
      """
        UPDATE hearing_day SET hearing_day = CASE
          WHEN id IN (6603, 6570, 6558) THEN DATE '2026-10-09'
          WHEN id IN (6569, 6556, 6559, 6563) THEN DATE '2026-10-22'
          WHEN id = 6557 THEN DATE '2026-10-08'
          ELSE DATE '2026-10-23'
        END
      """.trimIndent(),
    )
    jdbcTemplate.update(
      """
        UPDATE defendant SET c_id = CASE
          WHEN id IN (5812, 5837) THEN 'c12345'
          WHEN id = 5813 THEN 'c67890'
          WHEN id = 5838 THEN ''
          WHEN id = 5839 THEN '   '
          WHEN id = 5093 THEN 'common-platform-cid'
          ELSE NULL
        END
      """.trimIndent(),
    )
    jdbcTemplate.update("UPDATE defendant SET c_id = ? WHERE id = 5844", "\t\n")
  }

  @Test
  fun `should return common platform defendant IDs not CIDs`() {
    val result = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, null, pageable)

    assertThat(result.content).containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(result.totalElements).isEqualTo(1L)
  }

  @Test
  fun `should deduplicate libra CIDs across hearings and defendants and exclude blank CIDs`() {
    val result = repository.findLibraEnrichmentCandidates(fromDate, toDate, null, PageRequest.of(0, 1))

    assertThat(result.content).containsExactly("c12345")
    assertThat(result.totalElements).isEqualTo(1L)
    assertThat(result.totalPages).isEqualTo(1)
    assertThat(result.hasNext()).isFalse()
  }

  @Test
  fun `should include the start date and exclude the end date for both sources`() {
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, null, pageable).content)
      .containsExactly("c12345")
    assertThat(repository.findLibraEnrichmentCandidates(toDate, toDate.plusDays(1), null, pageable).content)
      .containsExactly("c67890")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, fromDate.plusDays(1), null, pageable).content)
      .containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate.minusDays(1), fromDate, null, pageable).content)
      .isEmpty()
  }

  @Test
  fun `should independently paginate distinct CIDs in deterministic order`() {
    val first = repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), null, PageRequest.of(0, 1))
    val second = repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), null, PageRequest.of(1, 1))
    val beyond = repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), null, PageRequest.of(2, 1))

    assertThat(first.content).containsExactly("c12345")
    assertThat(first.totalElements).isEqualTo(2L)
    assertThat(first.totalPages).isEqualTo(2)
    assertThat(first.hasNext()).isTrue()
    assertThat(second.content).containsExactly("c67890")
    assertThat(second.totalElements).isEqualTo(2L)
    assertThat(second.hasNext()).isFalse()
    assertThat(beyond.content).isEmpty()
    assertThat(beyond.totalElements).isEqualTo(2L)
    assertThat(beyond.totalPages).isEqualTo(2)
  }

  @Test
  fun `should paginate common platform IDs without counting repeated hearing days`() {
    jdbcTemplate.update("UPDATE court_case SET source_type = 'COMMON_PLATFORM' WHERE id = 7699")

    val first = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, null, PageRequest.of(0, 1))
    val second = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, null, PageRequest.of(1, 1))

    assertThat(first.content).containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(first.totalElements).isEqualTo(2L)
    assertThat(first.hasNext()).isTrue()
    assertThat(second.content).containsExactly("8dc4322f-75de-429b-875b-0063b7c0c044")
    assertThat(second.totalElements).isEqualTo(2L)
    assertThat(second.hasNext()).isFalse()
  }

  @Test
  fun `should filter by court code on the same matching hearing day`() {
    jdbcTemplate.update("UPDATE hearing_day SET court_code = 'B10JQ' WHERE id IN (6603, 6571)")

    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B10JQ", pageable).content)
      .containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B10JQ", pageable).content).isEmpty()
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), "B10JQ", pageable).content)
      .containsExactly("c67890")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO", pageable).content).isEmpty()
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO", pageable).content)
      .containsExactly("c12345")
  }

  @Test
  fun `should return empty pages with zero totals when no hearings match`() {
    val cp = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "UNKNOWN", pageable)
    val libra = repository.findLibraEnrichmentCandidates(fromDate, toDate, "UNKNOWN", pageable)

    assertThat(cp.content).isEmpty()
    assertThat(cp.totalElements).isZero()
    assertThat(cp.totalPages).isZero()
    assertThat(cp.hasNext()).isFalse()
    assertThat(libra.content).isEmpty()
    assertThat(libra.totalElements).isZero()
    assertThat(libra.totalPages).isZero()
    assertThat(libra.hasNext()).isFalse()
  }
}
