package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
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
import java.time.LocalDate
import javax.persistence.EntityManager

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

    @Test
    fun `should return outcomes resulted within last 14 days`() {
        val result = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome("B10JQ", HearingOutcomeSearchRequest(state = RESULTED))
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].first.hearingId).isEqualTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom {
            return HearingOutcomeRepositoryCustom(entityManager)
        }
    }
}