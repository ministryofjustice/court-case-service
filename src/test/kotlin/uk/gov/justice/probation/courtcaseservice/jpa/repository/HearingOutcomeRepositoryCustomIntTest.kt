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
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.RESULTED
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/hearing-outcomes.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
internal class HearingOutcomeRepositoryCustomIntTest {

    @Autowired
    lateinit var hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom

    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun `should return outcomes resulted within last 14 days`() {
        val result = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome("B10JQ", HearingOutcomeSearchRequest(
            state = RESULTED))
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].first.hearing.hearingId).isEqualTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
    }

    @Test
    fun `should apply court room filter`() {
        // Given
        var hearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager, 30)

        // When
        val result = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome("B10JQ",
            HearingOutcomeSearchRequest(state = RESULTED, courtRoom = listOf("1", "3")))

        // Then
        assertThat(result.size).isEqualTo(2)
        assertThat(result).extracting("first.hearing.hearingId").containsExactlyInAnyOrder("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636", "1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom {
            return HearingOutcomeRepositoryCustom(entityManager)
        }
    }
}