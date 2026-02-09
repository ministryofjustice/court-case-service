package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper

/**
 * Stub integration test for /db-seed and assert a case is created.
 */
@Sql(
  scripts = ["classpath:sql/before-seedController.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  executionPhase = ExecutionPhase.BEFORE_TEST_CLASS,
)
@Sql(
  scripts = ["classpath:after-test.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
)
@TestPropertySource(properties = ["db-seed.enabled=true"])
internal class SeedControllerIntTest : BaseIntTest() {

  @Autowired
  lateinit var courtCaseRepository: CourtCaseRepository

  @Test
  fun `should create single case when seeding endpoint invoked`() {
    courtCaseRepository.deleteAll()

    given()
      .auth().oauth2(TokenHelper.getToken())
      .post("/db-seed")
      .then()
      .statusCode(200)

    assertThat(courtCaseRepository.count()).isEqualTo(1)
  }
}
