package uk.gov.justice.probation.courtcaseservice.jpa.repository;

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
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.probation.courtcaseservice.jpa.DTOHelper.aHearingDefendantDTO
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-hearing-outcome-search.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
internal class HearingOutcomeRepositoryCustomDTOIntTest {
    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun `given Defendant and Offences exist, should return hearing defendant details`(){
        var hearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager, 30)

        val hearingDefendantDTO: HearingDefendantDTO = aHearingDefendantDTO(5944)
        // using null instead of a Hibernate Proxy object as a Lazy loaded proxy object
        hearingDefendantDTO.defendant = null
        hearingDefendantDTO.offences = null
        val result: HearingDefendantDTO = hearingOutcomeRepositoryCustom.getHearingDefendantDTO(hearingDefendantDTO);

        assertThat(result.offences.size).isEqualTo(1)
        assertThat(result.defendant).isNotNull
        assertThat(result.hearingOutcome).isEqualTo(hearingDefendantDTO.hearingOutcome)
    }

    @Test
    fun `given Offences does not exist, should return hearing defendant details`(){
        var hearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager, 30)

        val hearingDefendantDTO: HearingDefendantDTO = aHearingDefendantDTO(6000)
        // using null instead of a Hibernate Proxy object as a Lazy loaded proxy object
        hearingDefendantDTO.defendant = null
        hearingDefendantDTO.offences = null

        val result: HearingDefendantDTO = hearingOutcomeRepositoryCustom.getHearingDefendantDTO(hearingDefendantDTO);

        assertThat(result.offences.size).isEqualTo(0)
        assertThat(result).isEqualTo(hearingDefendantDTO)
        assertThat(result.hearingOutcome).isEqualTo(hearingDefendantDTO.hearingOutcome)
    }

    @TestConfiguration
    internal class TestConfig {
        @Bean
        fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom {
            return HearingOutcomeRepositoryCustom(entityManager)
        }
    }
}
