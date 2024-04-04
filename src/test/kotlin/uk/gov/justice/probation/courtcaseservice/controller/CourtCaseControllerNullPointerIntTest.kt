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
    scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-BugNullPointer.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)

internal class CourtCaseControllerNullPointerIntTest: BaseIntTest() {
    @Test
    fun `given hearing id and defendant id should return summary for defendant for that hearing`() {
        val hearingId = "c43c12e3-f8bc-4a07-bbce-63f9034ab360"
        val defendantId = "14a8d8d3-90db-4422-9e3e-920d7a26e2ad" // John Smith

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get("/hearing/{hearingId}/defendant/{defendantId}", hearingId, defendantId)
            .then()
            .statusCode(200)
            .body("caseId", equalTo("8a2f1cdc-b66e-4b1f-9fed-03089da8331b"))
            .body("hearings", hasSize<Any>(2))
            .log().all().extract().asString()
    }

    @Test
    fun `given hearing id and co defendant id should return summary for that co defendant for that hearing`() {
        val hearingId = "f57cf283-7a85-4c25-8412-4b73001e0b35" // First hearing. 2 Defendants
        val defendantId = "a25ddee6-68b0-4506-8c64-1f00b3644c61" // Dave Jones

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get("/hearing/{hearingId}/defendant/{defendantId}", hearingId, defendantId)
            .then()
            .statusCode(200)
            .body("caseId", equalTo("8a2f1cdc-b66e-4b1f-9fed-03089da8331b"))
            .body("hearings", hasSize<Any>(3))
            .log().all().extract().asString()
    }
}