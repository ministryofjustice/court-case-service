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
//        val defendantId = "f6e2482a-8230-4b4c-ab7d-716bd4000a5b"
//         val hearingId = "15cd65e6-eed1-4ecc-bd6b-37159d703733"
//        val hearingId = "b41ff816-5ff6-418a-a1f5-30a587830c03"

        val hearingId = "c43c12e3-f8bc-4a07-bbce-63f9034ab360"
        val defendantId = "14a8d8d3-90db-4422-9e3e-920d7a26e2ad"

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get("/hearing/{hearingId}/defendant/{defendantId}", hearingId, defendantId)
            .then()
            .statusCode(200)
            .log().all().extract().asString()
    }
}