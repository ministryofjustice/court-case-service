package uk.gov.justice.probation.courtcaseservice.jpa.repository

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.IN_PROGRESS
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.RESULTED

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/hearing-outcomes-dynamic-counts.sql"],
  config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class HearingOutcomeRepositoryDynamicCountsTest {
  @Autowired
  lateinit var hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom

  @Autowired
  lateinit var entityManager: EntityManager

  @Test
  fun `should return correct count for dynamic outcomes by state`() {
    // Given
    var hearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager, 30)

    // When
    val result = hearingOutcomeRepositoryCustom.getDynamicOutcomeCountsByState("B10JQ")

    // Then
    assertThat(result[RESULTED.name]).isEqualTo(2)
    assertThat(result[IN_PROGRESS.name]).isEqualTo(1)
  }

  @TestConfiguration
  internal class TestConfig {
    @Bean
    fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager)
  }
}
