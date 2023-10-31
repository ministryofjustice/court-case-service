package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
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
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class DefendantRepositoryIntTest {

    @Autowired
    lateinit var defendantRepository: DefendantRepository

    @Test
    fun shouldFindMatchingDefendants(){
        val matchingDefendants =
            defendantRepository.findMatchingDefendants("2004/0046583U", LocalDate.of(1975, 1, 1), "Jeff", "Blogs")

        assertThat(matchingDefendants.size).isEqualTo(1)
    }

    @Test
    fun shouldReturnEmptyWhenMatchingDefendantsNotFound(){
        val matchingDefendants =
            defendantRepository.findMatchingDefendants("2004/0046583U", LocalDate.of(1975, 1, 1), "J", "Blogs")

        assertThat(matchingDefendants).isEmpty()
    }

    @Test
    fun shouldReturnCorrectDefendantsForCRN(){
        val defendants = defendantRepository.findDefendantsByOffenderCrn("X375482")

        assertThat(defendants).hasSize(1)
    }
}