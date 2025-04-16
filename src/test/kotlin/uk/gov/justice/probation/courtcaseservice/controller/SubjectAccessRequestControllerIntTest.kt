package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.roles

/**
 * Sample test to check the service implementation is picked up by the endpoint and the service access request endpoint
 * is created.
 *
 * Also see SubjectAccessRequestServiceSampleTest for a sample service implementation.
 */
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-SubjectAccessRequestIntTest.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
)
@Sql(
  scripts = ["classpath:after-test.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
)
internal class SubjectAccessRequestControllerIntTest : BaseIntTest() {
  @Nested
  inner class Security {
    @BeforeEach
    fun setAuthRoles() {
      TokenHelper.cachedToken = null
    }

    @Test
    fun `access forbidden when no authority`() {
      RestAssured.given()
        .auth()
        .oauth2("null")
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=A12345")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `access forbidden when no role`() {
      roles = listOf()
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=A12345")
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `access forbidden with wrong role`() {
      roles = listOf("ROLE_BANANAS")
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=A12345")
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
    }
  }

  @Nested
  inner class SARContent {
    @BeforeEach
    fun setAuthRoles() {
      TokenHelper.cachedToken = null
      roles = listOf("ROLE_SAR_DATA_ACCESS")
    }

    @Test
    fun `should return non-draft and undeleted case comments, hearing outcomes and hearing notes if present for defendant`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .`when`()
        .get("/subject-access-request?crn=X25829")
        .then()
        .statusCode(200)
        .body("content.cases[0].caseId", Matchers.equalTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b"))
        .body("content.cases[0].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[0].comments[0].authorSurname", Matchers.equalTo("One"))
        .body("content.cases[0].comments[0].caseNumber", Matchers.equalTo("1600028888"))
        .body("content.cases[0].hearings[0].hearingId", Matchers.equalTo("5564cbfd-3d53-4f36-9508-437416b08738"))
        .body("content.cases[0].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[0].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[0].hearings[0].notes[0].authorSurname", Matchers.equalTo("Doe"))
        .body("content.cases[0].hearings[0].outcomes", Matchers.hasSize<Int>(1))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeType", Matchers.equalTo("Adjourned"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeDate", Matchers.equalTo("2023-04-24T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].resultedDate", Matchers.equalTo("2023-04-25T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].state", Matchers.equalTo("In progress"))
        .body("content.cases[0].hearings[0].outcomes[0].assignedTo", Matchers.equalTo("Smith"))
        .body("content.cases[0].hearings[0].outcomes[0].createdDate", Matchers.equalTo("2023-04-01T09:09:09"))
        .body("content.cases[1].caseId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings[0].hearingId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[1].hearings[0].notes[0].authorSurname", Matchers.equalTo("House"))
        .body("content.cases[1].hearings[0].outcomes", Matchers.hasSize<Int>(0))
        .body("content.cases[1].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[1].comments[0].authorSurname", Matchers.equalTo("Three"))
        .body("content.cases[1].comments[0].caseNumber", Matchers.equalTo("")) // No case number because it's source_type is CommonPlatform
    }

    @Test
    fun `should return case comments if present for defendant but no case number if not LIBRA`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=B25829")
        .then()
        .statusCode(200)
        .body("content.cases[0].comments[0].comment", Matchers.equalTo("PSR completed"))
        .body("content.cases[0].comments[0].authorSurname", Matchers.equalTo("Two"))
        .body("content.cases[0].comments[0].caseNumber", Matchers.equalTo(""))
        .body("content.cases[0].comments[0].created", Matchers.equalTo("2024-05-22T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdated", Matchers.equalTo("2024-04-09T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor2"))
    }

    @Test
    fun `should not return case comments if not present for defendant`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=Z258210")
        .then()
        .statusCode(204)
    }

    @Test
    fun `should return case comments, hearing notes and hearing outcomes if between valid date ranges`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=X25829&fromDate=2022-10-10")
        .then()
        .statusCode(200)
        .body("content.cases[0].caseId", Matchers.equalTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b"))
        .body("content.cases[0].hearings[0].hearingId", Matchers.equalTo("5564cbfd-3d53-4f36-9508-437416b08738"))
        .body("content.cases[0].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[0].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[0].hearings[0].notes[0].authorSurname", Matchers.equalTo("Doe"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeType", Matchers.equalTo("Adjourned"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeDate", Matchers.equalTo("2023-04-24T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].resultedDate", Matchers.equalTo("2023-04-25T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].state", Matchers.equalTo("In progress"))
        .body("content.cases[0].hearings[0].outcomes[0].assignedTo", Matchers.equalTo("Smith"))
        .body("content.cases[0].hearings[0].outcomes[0].createdDate", Matchers.equalTo("2023-04-01T09:09:09"))
        .body("content.cases[0].comments", Matchers.hasSize<Int>(1))
        .body("content.cases[0].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[0].comments[0].authorSurname", Matchers.equalTo("One"))
        .body("content.cases[0].comments[0].created", Matchers.equalTo("2024-05-21T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdated", Matchers.equalTo("2024-04-08T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor"))
        .body("content.cases[0].comments[0].caseNumber", Matchers.equalTo("1600028888"))
        .body("content.cases[1].caseId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].hearingId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[1].hearings[0].notes[0].authorSurname", Matchers.equalTo("House"))
        .body("content.cases[1].hearings[0].outcomes", Matchers.hasSize<Int>(0))
        .body("content.cases[1].comments", Matchers.hasSize<Int>(1))
        .body("content.cases[1].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[1].comments[0].authorSurname", Matchers.equalTo("Three"))
        .body("content.cases[1].comments[0].created", Matchers.equalTo("2024-04-21T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdated", Matchers.equalTo("2024-03-08T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor3"))
        .body("content.cases[1].comments[0].caseNumber", Matchers.equalTo(""))
    }

    @Test
    fun `given case comments present but hearing notes and outcomes not present, should return case comments and no hearings for defendant from date`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=X25829&fromDate=2024-05-21")
        .then()
        .statusCode(200)
        .body("content.cases[0].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[0].comments[0].authorSurname", Matchers.equalTo("One"))
        .body("content.cases[0].comments[0].created", Matchers.equalTo("2024-05-21T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdated", Matchers.equalTo("2024-04-08T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor"))
        .body("content.cases[0].comments[0].caseNumber", Matchers.equalTo("1600028888"))
        .body("content.cases[0].hearings", Matchers.empty<Any>())
    }

    @Test
    fun `should return hearing notes and hearing outcomes if present for defendant from date`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=X25829&fromDate=2022-01-01")
        .then()
        .statusCode(200)
        .body("content.cases[0].caseId", Matchers.equalTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b"))
        .body("content.cases[0].hearings[0].hearingId", Matchers.equalTo("5564cbfd-3d53-4f36-9508-437416b08738"))
        .body("content.cases[0].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[0].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[0].hearings[0].notes[0].authorSurname", Matchers.equalTo("Doe"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeType", Matchers.equalTo("Adjourned"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeDate", Matchers.equalTo("2023-04-24T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].resultedDate", Matchers.equalTo("2023-04-25T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].state", Matchers.equalTo("In progress"))
        .body("content.cases[0].hearings[0].outcomes[0].assignedTo", Matchers.equalTo("Smith"))
        .body("content.cases[0].hearings[0].outcomes[0].createdDate", Matchers.equalTo("2023-04-01T09:09:09"))
        .body("content.cases[0].comments", Matchers.hasSize<Int>(1))
        .body("content.cases[0].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[0].comments[0].authorSurname", Matchers.equalTo("One"))
        .body("content.cases[0].comments[0].created", Matchers.equalTo("2024-05-21T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdated", Matchers.equalTo("2024-04-08T09:45:55.597"))
        .body("content.cases[0].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor"))
        .body("content.cases[0].comments[0].caseNumber", Matchers.equalTo("1600028888"))
        .body("content.cases[1].caseId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].hearingId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[1].hearings[0].notes[0].authorSurname", Matchers.equalTo("House"))
        .body("content.cases[1].hearings[0].outcomes", Matchers.hasSize<Int>(0))
        .body("content.cases[1].comments", Matchers.hasSize<Int>(1))
        .body("content.cases[1].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[1].comments[0].authorSurname", Matchers.equalTo("Three"))
        .body("content.cases[1].comments[0].created", Matchers.equalTo("2024-04-21T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdated", Matchers.equalTo("2024-03-08T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor3"))
        .body("content.cases[1].comments[0].caseNumber", Matchers.equalTo(""))
    }

    @Test
    fun `should return case comments, hearing outcomes and hearing notes if present for defendant to date`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=X25829&toDate=2024-04-25")
        .then()
        .statusCode(200)
        .body("content.cases[0].caseId", Matchers.equalTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b"))
        .body("content.cases[0].hearings[0].hearingId", Matchers.equalTo("5564cbfd-3d53-4f36-9508-437416b08738"))
        .body("content.cases[0].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[0].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[0].hearings[0].notes[0].authorSurname", Matchers.equalTo("Doe"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeType", Matchers.equalTo("Adjourned"))
        .body("content.cases[0].hearings[0].outcomes[0].outcomeDate", Matchers.equalTo("2023-04-24T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].resultedDate", Matchers.equalTo("2023-04-25T09:09:09"))
        .body("content.cases[0].hearings[0].outcomes[0].state", Matchers.equalTo("In progress"))
        .body("content.cases[0].hearings[0].outcomes[0].assignedTo", Matchers.equalTo("Smith"))
        .body("content.cases[0].hearings[0].outcomes[0].createdDate", Matchers.equalTo("2023-04-01T09:09:09"))
        .body("content.cases[0].comments", Matchers.hasSize<Int>(0)) // deleted comments aren't returned
        .body("content.cases[1].caseId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].hearingId", Matchers.equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
        .body("content.cases[1].hearings[0].notes", Matchers.hasSize<Int>(1))
        .body("content.cases[1].hearings[0].notes[0].note", Matchers.equalTo("This is a test comment by the Prepare a case digital team."))
        .body("content.cases[1].hearings[0].notes[0].authorSurname", Matchers.equalTo("House"))
        .body("content.cases[1].hearings[0].outcomes", Matchers.hasSize<Int>(0))
        .body("content.cases[1].comments", Matchers.hasSize<Int>(1))
        .body("content.cases[1].comments[0].comment", Matchers.equalTo("PSR in progress"))
        .body("content.cases[1].comments[0].authorSurname", Matchers.equalTo("Three"))
        .body("content.cases[1].comments[0].created", Matchers.equalTo("2024-04-21T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdated", Matchers.equalTo("2024-03-08T09:45:55.597"))
        .body("content.cases[1].comments[0].lastUpdatedBy", Matchers.equalTo("LastUpdatedAuthor3"))
        .body("content.cases[1].comments[0].caseNumber", Matchers.equalTo(""))
    }

    @Test
    fun `should return no case comments, hearing outcomes and hearing notes if not present for defendant to date`() {
      RestAssured.given()
        .auth()
        .oauth2(getToken())
        .`when`()
        .header(
          "Accept",
          "application/json",
        )
        .get("/subject-access-request?crn=X25829&toDate=2022-10-09")
        .then()
        .statusCode(204)
    }
  }
}
