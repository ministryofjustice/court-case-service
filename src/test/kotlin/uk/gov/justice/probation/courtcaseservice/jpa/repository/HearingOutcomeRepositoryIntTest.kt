package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-SubjectAccessRequestIntTest.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class HearingOutcomeRepositoryIntTest {
    @Autowired
    private lateinit var hearingOutcomeRepository: HearingOutcomeRepository;

    @Autowired
    lateinit var hearingDefendantRepository: HearingDefendantRepository;

    lateinit var hearingDefendantEntity: HearingDefendantEntity;

    lateinit var fromDateTime: LocalDateTime;
    lateinit var toDateTime: LocalDateTime;

    @BeforeEach
    fun initTest() {
        hearingDefendantEntity = hearingDefendantRepository.findByIdOrNull(-198)!!
        fromDateTime = LocalDateTime.parse("2023-04-01T00:00:00")
        toDateTime =  LocalDateTime.parse("2023-04-02T00:00:00")
    }

    @Test
    fun `given hearing defendant and valid date ranges, it should find matching hearing outcomes`(){
        val matchingHearingOutcomes = hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBetween(hearingDefendantEntity.id, fromDateTime, toDateTime)

        assertThat(matchingHearingOutcomes.size).isEqualTo(1)
    }

    @Test
    fun `given hearing defendant and valid fromDate, it should find matching hearing outcomes`(){
        val matchingHearingOutcomes =
            hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedAfter(hearingDefendantEntity.id, fromDateTime)

        assertThat(matchingHearingOutcomes.size).isEqualTo(1)
    }

    @Test
    fun `given hearing defendant and valid toDate, it should find matching hearing outcomes`(){
        val matchingHearingOutcomes =
            hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBefore(hearingDefendantEntity.id, toDateTime)

        assertThat(matchingHearingOutcomes.size).isEqualTo(1)
    }

    @Test
    fun `given hearing defendant and no dates, it should find matching hearing outcomes`(){
        val matchingHearingOutcomes = hearingOutcomeRepository.findByHearingDefendantId(hearingDefendantEntity.id)

        assertThat(matchingHearingOutcomes.size).isEqualTo(1)
    }

    @Test
    fun `given hearing defendant and hearing notes out of valid minimum date range, it should find no matching hearing outcomes`(){
        val matchingHearingOutcomes = hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedAfter(hearingDefendantEntity.id, fromDateTime.plusDays(5))

        assertThat(matchingHearingOutcomes.size).isEqualTo(0)
    }

    @Test
    fun `given hearing defendant and hearing outcomes out of valid maximum date range, it should find no matching hearing outcomes`(){
        val matchingHearingOutcomes = hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBefore(hearingDefendantEntity.id, toDateTime.minusDays(1))

        assertThat(matchingHearingOutcomes.size).isEqualTo(0)
    }
}
