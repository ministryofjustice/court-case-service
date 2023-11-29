package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusOffenderDetail
import uk.gov.justice.probation.courtcaseservice.client.model.Identifiers
import uk.gov.justice.probation.courtcaseservice.client.model.Name
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository
import java.time.LocalDate

@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-new-offender-event-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
internal class ProbationCaseEngagementServiceIntTest: BaseIntTest() {

    @Autowired
    lateinit var defendantRepository: DefendantRepository

    @Autowired
    lateinit var engagementService: ProbationCaseEngagementService

    @Autowired
    lateinit var offenderRepository: OffenderRepository

    @Autowired
    lateinit var hearingRepository: HearingRepository


    @Test
    fun testIt() {

        var expectedMatchingDefendantsAfterUpdate =
            defendantRepository.findMatchingDefendants("PN/1234560XX", LocalDate.of(1939, 10, 10), "David", "BOWIE")
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate.size).isEqualTo(2)
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate[0].offender).isNull()
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate[1].offender).isNull()

        testMethod();

        expectedMatchingDefendantsAfterUpdate =
            defendantRepository.findMatchingDefendants("PN/1234560XX", LocalDate.of(1939, 10, 10), "David", "BOWIE")
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate.size).isEqualTo(2)
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate[0].offender).isNotNull
        Assertions.assertThat(expectedMatchingDefendantsAfterUpdate[1].offender).isNotNull

    }

    @Transactional
    fun testMethod() {
        engagementService.updateMatchingDefendantsWithOffender(DeliusOffenderDetail(
            Name("David", "BOWIE"),
            Identifiers("XXX1234","PN/1234560XX"),
            LocalDate.of(1939, 10, 10)
        ))
    }
}
