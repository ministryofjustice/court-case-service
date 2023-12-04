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
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingStatus


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

    @Test
    fun shouldReturnHeardCasesCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 5, page = 1,
                hearingStatus = HearingStatus.HEARD
            )
        )
        assertThat(page1.content.size).isEqualTo(2)
        assertThat(page1.totalElements).isEqualTo(2)
        assertThat(page1.totalPages).isEqualTo(1)
        assertThat(page1.content.map { it.first.hearing.hearingId }).isEqualTo(listOf("cbafcebb-3430-4710-8557-5c93bd1e8be5", "0e6c7d7e-7057-45db-b788-210df7a9a624"))
    }

    @Test
    fun shouldReturnUnHeardCasesCorrectly() {
        val page1 = pagedCaseListRepositoryCustom.filterHearings(
            "B14LO",
            HearingSearchRequest(
                date = LocalDate.of(2023, 7, 3), size = 20, page = 1,
                hearingStatus = HearingStatus.UNHEARD
            )
        )
        assertThat(page1.content.size).isEqualTo(9)
        assertThat(page1.totalElements).isEqualTo(9)
        assertThat(page1.totalPages).isEqualTo(1)

        assertThat(page1.content.map { it.first.hearing.hearingId }.sorted()).isEqualTo(listOf(
            "57e86555-bd97-43f7-ad1c-55a992b37a2d",
            "1eb3a6da-8189-4de2-8377-da5910e486b9",
            "4a7220b8-88bc-4417-8ee0-cfc318047b3c",
            "85f400a9-82c9-4a9d-93ec-066d55be0c07",
            "79c176bf-a6ff-4f82-afba-de136aae1536",
            "af8fa3b5-d544-4c70-b4f4-3d8639197d4b",
            "a9d0f014-3fde-41a8-a416-2dabc9e21bae",
            "5a173167-5d34-4112-b563-afb1067d229d",
            "eae601d7-3966-494f-a8bb-bb23989cfd6f",
        ).sorted())
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): PagedCaseListRepositoryCustom {
            return PagedCaseListRepositoryCustom(entityManager)
        }
    }
}