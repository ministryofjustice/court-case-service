package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper

@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:case-progress.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
internal class CaseWorkflowControllerIntTest: BaseIntTest() {

    companion object {
        val HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00"
        val hearingOutcomeRequest: String = "{ \"hearingOutcomeType\": \"ADJOURNED\" }"
    }

    @Autowired
    lateinit var hearingRepository: HearingRepository


    @Test
    fun `given hearing id and outcome should record hearing outcome`() {

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingOutcomeRequest)
            .`when`()
            .put("/hearing/{hearingId}/outcome", HEARING_ID)
            .then()
            .statusCode(200)

        var hearing = hearingRepository.findFirstByHearingId(HEARING_ID).get();
        assertThat(HearingOutcome.of(null)).isNull()

        assertThat(hearing.hearingOutcome.outcomeType).isEqualTo("ADJOURNED");
    }
}