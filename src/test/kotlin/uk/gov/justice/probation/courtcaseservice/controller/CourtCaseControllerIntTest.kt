package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper

@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:case-progress.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)

internal class CourtCaseControllerDefendantIntTest: BaseIntTest() {
    @Test
    fun `given hearing id and defendant id should return summary for defendant for that hearing`() {
        val defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002"
        val hearingId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00"

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(CaseWorkflowControllerIntTest.HEARING_OUTCOME_REQUEST)
            .`when`()
            .get("/hearing/{hearingId}/defendant/{defendantId}", hearingId, defendantId)
            .then()
            .statusCode(200)
            .body("hearings", hasSize<Any>(3))
            .body("hearings[1].hearingOutcome.assignedTo", equalTo("Joe Blogs"))
            .log().all().extract().asString()
    }
}