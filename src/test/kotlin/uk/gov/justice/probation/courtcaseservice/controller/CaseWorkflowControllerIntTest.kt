package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
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
        val HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00"
        val UNKNOWN_HEARING_ID = "111111-1111-1111-1111-111111111111"
        val hearingOutcomeRequest: String = "{ \"hearingOutcomeType\": \"ADJOURNED\" }"
        val hearingOutcomeAssignRequest: String = "{ \"assignedTo\": \"John Smith\" }"
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
        assertThat(hearing.hearingOutcome.outcomeType).isEqualTo("ADJOURNED")
        assertThat(hearing.hearingOutcome.outcomeDate).isNotNull()
    }

    @Test
    fun `given court code and outcome state NEW and filters should return all outcomes for that court`() {

        var courtCode = "B33HU"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
            .queryParam("state", "NEW")
            .queryParam("outcomeType",  "PROBATION_SENTENCE", "ADJOURNED")
            .queryParam("sortBy",  "hearingDate")
            .queryParam("order",  "DESC")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingOutcomeRequest)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .body("cases", hasSize<Any>(1))
            .body("cases[0].hearingOutcomeType", equalTo("ADJOURNED"))
            .body("cases[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
            .body("cases[0].hearingId", equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("cases[0].hearingDate", equalTo("2019-10-14"))
            .body("cases[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
            .body("cases[0].defendantName", equalTo("Mr Johnny BALL"))
            .body("cases[0].offences", equalTo(listOf("Theft from a different shop", "Theft from a shop")))
            .body("cases[0].probationStatus", equalTo("Current"))
    }

    @Test
    fun `given court code and outcome state IN_PROGRESS return all outcomes for that court`() {

        var courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
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
                .body("cases", hasSize<Any>(2))
                .body("cases[0].hearingOutcomeType", equalTo("ADJOURNED"))
                .body("cases[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
                .body("cases[0].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("cases[0].hearingDate", equalTo("2019-11-14"))
                .body("cases[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
                .body("cases[0].defendantName", equalTo("Mr Johnny BALL"))
                .body("cases[0].probationStatus", equalTo("Current"))
                .body("cases[0].assignedTo", equalTo("John Smith"))
                .body("cases[0].assignedToUuid", equalTo("8f69def4-3c52-11ee-be56-0242ac120002"))

                .body("cases[1].hearingOutcomeType", equalTo("ADJOURNED"))
                .body("cases[1].outcomeDate", equalTo("2023-04-24T09:09:09"))
                .body("cases[1].hearingId", equalTo("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"))
                .body("cases[1].hearingDate", equalTo("2019-12-14"))
                .body("cases[1].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
                .body("cases[1].defendantName", equalTo("Mr Johnny BALL"))
                .body("cases[1].probationStatus", equalTo("Current"))
                .body("cases[1].assignedTo", equalTo("Joe Blogs"))
                .body("cases[1].assignedToUuid", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
    }

    @Test
    fun `given court code and assigned to uuid then return all outcomes assigned to that user id`() {

        var courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
            .queryParam("assignedToUuid", "4b03d065-4c96-4b24-8d6d-75a45d2e3f12")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .accept(ContentType.JSON)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .body("cases", hasSize<Any>(1))
            .body("cases[0].hearingOutcomeType", equalTo("ADJOURNED"))
            .body("cases[0].outcomeDate", equalTo("2023-04-24T09:09:09"))
            .body("cases[0].hearingId", equalTo("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"))
            .body("cases[0].hearingDate", equalTo("2019-12-14"))
            .body("cases[0].defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
            .body("cases[0].defendantName", equalTo("Mr Johnny BALL"))
            .body("cases[0].probationStatus", equalTo("Current"))
            .body("cases[0].assignedTo", equalTo("Joe Blogs"))
            .body("cases[0].assignedToUuid", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
    }

    @Test
    fun `given court code and outcome state NEW and filters do not match should return empty response`() {

        var courtCode = "B33HU"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
            .queryParam("state", "NEW")
            .queryParam("outcomeType",  "PROBATION_SENTENCE", "REPORT_REQUESTED")
            .build().toUriString()

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingOutcomeRequest)
            .`when`()
            .get(endpoint)
            .then()
            .statusCode(200)
            .body("cases", hasSize<Any>(0))
    }

    @Test
    fun `given a known hearing id and assign to string should update hearing outcome and return a 200 response `() {
        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(hearingOutcomeAssignRequest)
                .`when`()
                .put("/hearing/{hearingId}/outcome/assign", HEARING_ID)
                .then()
                .statusCode(200)

        var hearing = hearingRepository.findFirstByHearingId(HEARING_ID).get();
        assertThat(hearing.hearingOutcome.assignedTo).isEqualTo("John Smith")
        assertThat(hearing.hearingOutcome.assignedToUuid).isEqualTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81")
    }

    @Test
    fun `given an unknown hearing id and assign to string should return a 404 response `() {
        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(hearingOutcomeAssignRequest)
                .`when`()
                .put("/hearing/{hearingId}/outcome/assign", UNKNOWN_HEARING_ID)
                .then()
                .statusCode(404)

    }
}