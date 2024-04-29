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
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/hearing-outcomes-pagination.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
internal class HearingOutcomeRepositoryCustomPaginationIntTest {

    @Autowired
    lateinit var hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom

    @Test
    fun `given court code it should return all HearingDefendants with outcomes for this court`() {
        val result = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome("B10JQ", HearingOutcomeSearchRequest())
        assertThat(result.size).isEqualTo(3)
        assertThat(result[0].first.hearing.hearingId).isEqualTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7")
        assertThat(result[1].first.hearing.hearingId).isEqualTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
        assertThat(result[2].first.hearing.hearingId).isEqualTo("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636")
    }

    @Test
    fun `given court code and search filters it should return filtered HearingDefendants with outcomes for this court`() {
        val result = hearingOutcomeRepositoryCustom.findByCourtCodeAndHearingOutcome("B10JQ", HearingOutcomeSearchRequest(
            state = IN_PROGRESS, assignedToUuid = listOf("8f69def4-3c52-11ee-be56-0242ac120002")))
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].first.hearingOutcome.assignedTo).isEqualTo("John Smith")
        assertThat(result[0].first.hearingOutcome.assignedToUuid).isEqualTo("8f69def4-3c52-11ee-be56-0242ac120002")
        assertThat(result[0].first.hearing.hearingId).isEqualTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom {
            return HearingOutcomeRepositoryCustom(entityManager)
        }
    }
}