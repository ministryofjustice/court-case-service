package uk.gov.justice.probation.courtcaseservice.controller.v2

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper
import java.net.URI


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
        const val HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00"
        const val DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002"
        const val UNKNOWN_HEARING_ID = "111111-1111-1111-1111-111111111111"
        const val HEARING_OUTCOME_REQUEST: String = "{ \"hearingOutcomeType\": \"ADJOURNED\" }"
        const val HEARING_OUTCOME_UPDATE_REQUEST: String = "{ \"hearingOutcomeType\": \"REPORT_REQUESTED\" }"
        const val HEARING_OUTCOME_ASSIGN_REQUEST: String = "{ \"assignedTo\": \"John Smith\" }"
    }

    @Autowired
    lateinit var hearingRepository: HearingRepository

    @SpyBean
    lateinit var caseWorkflowService: CaseWorkflowService

    @Test
    fun `V2 given court code and outcome state NEW and filters should return all outcomes for that court with pagination headers`() {

        val courtCode = "B33HU"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
            .queryParam("hearingOutcomeState", "NEW")
            .queryParam("hearingOutcomeOutcomeType",  "PROBATION_SENTENCE", "ADJOURNED")
            .queryParam("sortBy",  "hearingDate")
            .queryParam("order",  "DESC")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .header("X-PAGINATION-CURRENT-PAGE", equalTo("1"))
            .header("X-PAGINATION-PAGE-SIZE", equalTo("20"))
            .header("X-PAGINATION-TOTAL-PAGES", equalTo("1"))
            .header("X-PAGINATION-TOTAL-RESULTS", equalTo("1"))
            .body("records", hasSize<Any>(1))
            .body("records[0].hearingOutcomeType", equalTo("ADJOURNED"))
            .body("records[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
            .body("records[0].hearingId", equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("records[0].hearingDate", equalTo("2019-10-14"))
            .body("records[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
            .body("records[0].defendantName", equalTo("Mr Johnny BALL"))
            .body("records[0].offences", equalTo(listOf("Theft from a different shop", "Theft from a shop")))
            .body("records[0].probationStatus", equalTo("Current"))
            .body("filters[0]['name']", equalTo("Assigned Users"))
            .body("filters[0]['id']", equalTo("assignedUsers"))
            .body("filters[0]['items'][0]['id']", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
            .body("filters[0]['items'][0]['name']", equalTo("Joe Blogs"))
            .body("filters[2]['id']", equalTo("states"))
            .body("filters[2]['name']", equalTo("Hearing Outcome States"))
            .body("filters[2]['items'][0]['id']", equalTo("NEW"))
            .body("filters[2]['items'][0]['matches']", equalTo(1))
    }

    @Test
    fun `given court code and outcome state IN_PROGRESS return all outcomes for that court`() {

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
                .queryParam("state", "IN_PROGRESS")
                .build().toUriString()

        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .accept(ContentType.JSON)
                .`when`()
                .get(endpoint)
                .then()
                .statusCode(200)
                .header("X-PAGINATION-CURRENT-PAGE", equalTo("1"))
                .header("X-PAGINATION-PAGE-SIZE", equalTo("20"))
                .header("X-PAGINATION-TOTAL-PAGES", equalTo("1"))
                .header("X-PAGINATION-TOTAL-RESULTS", equalTo("2"))
                .body("records", hasSize<Any>(2))
                .body("records[0].hearingOutcomeType", equalTo("ADJOURNED"))
                .body("records[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
                .body("records[0].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("records[0].hearingDate", equalTo("2019-11-14"))
                .body("records[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
                .body("records[0].defendantName", equalTo("Mr Johnny BALL"))
                .body("records[0].probationStatus", equalTo("Current"))
                .body("records[0].assignedTo", equalTo("John Smith"))
                .body("records[0].assignedToUuid", equalTo("8f69def4-3c52-11ee-be56-0242ac120002"))
                .body("filters[0]['name']", equalTo("Assigned Users"))
                .body("filters[0]['id']", equalTo("assignedUsers"))
                .body("filters[0]['items'][0]['id']", equalTo("8f69def4-3c52-11ee-be56-0242ac120002"))
                .body("filters[0]['items'][0]['name']", equalTo("John Smith"))
                .body("filters[0]['items'][0]['active']", equalTo(true))
                .body("filters[2]['id']", equalTo("states"))
                .body("filters[2]['name']", equalTo("Hearing Outcome States"))
                .body("filters[2]['items'][0]['id']", equalTo("NEW"))
                .body("filters[2]['items'][0]['matches']", equalTo(0))
                .body("filters[2]['items'][1]['id']", equalTo("IN_PROGRESS"))
                .body("filters[2]['items'][1]['matches']", equalTo(2))
    }

    @Test
    fun `given court code and outcome state IN_PROGRESS and assigned to user, should return outcomes correctly`() {

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
                .queryParam("state", "IN_PROGRESS")
                .queryParam("assignedUsers", listOf("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
                .build().toUriString()

        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .accept(ContentType.JSON)
                .`when`()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("records", hasSize<Any>(1))
                .body("records[0].hearingOutcomeType", equalTo("ADJOURNED"))
                .body("records[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
                .body("records[0].hearingId", equalTo("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"))
                .body("records[0].hearingDate", equalTo("2019-12-14"))
                .body("records[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
                .body("records[0].defendantName", equalTo("Mr Johnny BALL"))
                .body("records[0].probationStatus", equalTo("Current"))
                .body("records[0].assignedTo", equalTo("Joe Blogs"))
                .body("records[0].assignedToUuid", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
                .body("records[0].state", equalTo("IN_PROGRESS"))
    }

    @Test
    fun `given court code and assigned user then return all outcomes assigned to that user`() {

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
            .queryParam("assignedUsers", listOf("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .accept(ContentType.JSON)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .body("records", hasSize<Any>(1))
            .body("records[0].hearingOutcomeType", equalTo("ADJOURNED"))
            .body("records[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
            .body("records[0].hearingId", equalTo("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"))
            .body("records[0].hearingDate", equalTo("2019-12-14"))
            .body("records[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
            .body("records[0].defendantName", equalTo("Mr Johnny BALL"))
            .body("records[0].crn", equalTo("X320741"))
            .body("records[0].probationStatus", equalTo("Current"))
            .body("records[0].assignedTo", equalTo("Joe Blogs"))
            .body("records[0].assignedToUuid", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
    }

    @Test
    fun `given court code and outcome state NEW and filters do not match, it should return an empty response`() {

        val courtCode = "B33HU"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
            .queryParam("state", "NEW")
            .queryParam("outcomeTypes",  "PROBATION_SENTENCE", "REPORT_REQUESTED")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(HEARING_OUTCOME_REQUEST)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .header("X-PAGINATION-CURRENT-PAGE", equalTo("1"))
            .header("X-PAGINATION-PAGE-SIZE", equalTo("20"))
            .header("X-PAGINATION-TOTAL-PAGES", equalTo("0"))
            .header("X-PAGINATION-TOTAL-RESULTS", equalTo("0"))
            .body("records", hasSize<Any>(0))
            .body("filters[0]['name']", equalTo("Assigned Users"))
            .body("filters[0]['id']", equalTo("assignedUsers"))
            .body("filters[0]['items']", hasSize<Any>(0))
            .body("filters[1]['id']", equalTo("courtRooms"))
            .body("filters[1]['items']", hasSize<Any>(1))
            .body("filters[1]['items'][0]['id']", equalTo("2"))
            .body("filters[2]['id']", equalTo("states"))
            .body("filters[2]['name']", equalTo("Hearing Outcome States"))
            .body("filters[2]['items'][0]['id']", equalTo("NEW"))
            .body("filters[2]['items'][0]['matches']", equalTo(1))
            .body("filters[2]['items'][1]['id']", equalTo("IN_PROGRESS"))
            .body("filters[2]['items'][1]['matches']", equalTo(0))
    }

    @Test
    fun `given court code, soryBy and order, it should return all hearing defendant outcomes in hearingDate sort and descending order`(){

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
            .queryParam("sortBy",  "hearingDate")
            .queryParam("order",  "DESC")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .header("X-PAGINATION-CURRENT-PAGE", equalTo("1"))
            .header("X-PAGINATION-PAGE-SIZE", equalTo("20"))
            .header("X-PAGINATION-TOTAL-PAGES", equalTo("1"))
            .header("X-PAGINATION-TOTAL-RESULTS", equalTo("2"))
            .body("records", hasSize<Any>(2))
            .body("records[0].hearingDate", equalTo("2019-12-14"))
            .body("records[1].hearingDate", equalTo("2019-11-14"))
    }

    @Test
    fun `given court code, soryBy and order, it should return all hearing defendant outcomes in hearingDate sort and ascending order`(){

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("v2/courts/${courtCode}/hearing-defendant-outcomes"))
            .queryParam("sortBy",  "hearingDate")
            .queryParam("order",  "ASC")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .header("X-PAGINATION-CURRENT-PAGE", equalTo("1"))
            .header("X-PAGINATION-PAGE-SIZE", equalTo("20"))
            .header("X-PAGINATION-TOTAL-PAGES", equalTo("1"))
            .header("X-PAGINATION-TOTAL-RESULTS", equalTo("2"))
            .body("records", hasSize<Any>(2))
            .body("records[0].hearingDate", equalTo("2019-11-14"))
            .body("records[1].hearingDate", equalTo("2019-12-14"))
    }
}