package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSearchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import java.time.LocalDate
import jakarta.persistence.EntityManager


@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-new-hearing-search.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
class PagedCaseListRepositoryCustomIntTest {

    @Autowired
    lateinit var pagedCaseListRepositoryCustom: PagedCaseListRepositoryCustom

    @Test
    fun shouldReturnPageOneResultCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings("B14LO", HearingSearchRequest(date = LocalDate.of(2023, 7, 3), page = 1, size = 5))
        assertThat(page1.content.size).isEqualTo(5)
        assertThat(page1.totalElements).isEqualTo(11)
        assertThat(page1.totalPages).isEqualTo(3)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Jeff Blogs", "Mr Jeff Blogs", "Mr Cloud Strife", "Miss. Portia Labiata", "Mr Bare Knuckles"))
    }

    @Test
    fun shouldReturnPageTwoResultCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings("B14LO", HearingSearchRequest(date = LocalDate.of(2023, 7, 3), size = 5, page = 2))
        assertThat(page1.content.size).isEqualTo(5)
        assertThat(page1.totalElements).isEqualTo(11)
        assertThat(page1.totalPages).isEqualTo(3)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(
            listOf("Mr Clifford Li",
                "Miss Esther Egge",
                "Mrs Mary Berry",
                "Mrs Lagertha Lothbrok",
                "Block BUSTERS"))
    }

    @Test
    fun shouldReturnPageThreeResultCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings("B14LO", HearingSearchRequest(date = LocalDate.of(2023, 7, 3), size = 5, page = 3))
        assertThat(page1.content.size).isEqualTo(1)
        assertThat(page1.totalElements).isEqualTo(11)
        assertThat(page1.totalPages).isEqualTo(3)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Arthur Morgan"))
    }

    @Test
    fun shouldReturnEmptyPageWhenOnNoResults() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings("XXXXX", HearingSearchRequest(date = LocalDate.of(2023, 7, 3), size = 5, page = 1))
        assertThat(page1.content.size).isEqualTo(0)
        assertThat(page1.totalElements).isEqualTo(0)
        assertThat(page1.totalPages).isEqualTo(0)
    }

    @Test
    fun shouldApplyProbationStatusFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                probationStatus = mutableListOf("CURRENT", "Possible NDelius record")
            )
        )
        assertThat(page1.content.size).isEqualTo(5)
        assertThat(page1.totalElements).isEqualTo(5)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Jeff Blogs", "Mr Jeff Blogs", "Mr Cloud Strife", "Mr Clifford Li", "Mr Arthur Morgan"))
    }

    @Test
    fun shouldApplyCourtRoomFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                probationStatus = mutableListOf("CURRENT", "Possible NDelius record"),
                courtRoom = listOf("03", "04", "05")
            )
        )
        assertThat(page1.content.size).isEqualTo(2)
        assertThat(page1.totalElements).isEqualTo(2)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Cloud Strife", "Mr Clifford Li"))
    }

    @Test
    fun shouldApplySessionFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                courtRoom = listOf("05"),
                session = listOf(CourtSession.AFTERNOON)
            )
        )
        assertThat(page1.content.size).isEqualTo(2)
        assertThat(page1.totalElements).isEqualTo(2)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mrs Mary Berry", "Mrs Lagertha Lothbrok"))
    }

    @Test
    fun shouldApplySourceFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                source = listOf(SourceType.COMMON_PLATFORM)
            )
        )
        assertThat(page1.content.size).isEqualTo(1)
        assertThat(page1.totalElements).isEqualTo(1)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Arthur Morgan"))
    }

    @Test
    fun shouldApplyBreachFlagFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                breach = true
            )
        )
        assertThat(page1.content.size).isEqualTo(1)
        assertThat(page1.totalElements).isEqualTo(1)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Arthur Morgan"))
    }

    @Test
    fun shouldApplyRecentlyAddedFilterCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                recentlyAdded = true
            )
        )
        assertThat(page1.content.size).isEqualTo(2)
        assertThat(page1.totalElements).isEqualTo(2)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.defendant.defendantName }).isEqualTo(listOf("Mr Jeff Blogs", "Miss Esther Egge"))
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): PagedCaseListRepositoryCustom {
            return PagedCaseListRepositoryCustom(entityManager)
        }
    }
}