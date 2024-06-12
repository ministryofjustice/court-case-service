package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
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
import uk.gov.justice.probation.courtcaseservice.service.HearingNotesServiceInitService
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
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

    @Autowired
    lateinit var hearingNotesServiceInitService: HearingNotesServiceInitService

    @SpyBean
    lateinit var caseWorkflowService: CaseWorkflowService

    @Test
    fun `given hearing id and outcome should record hearing outcome`() {

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(HEARING_OUTCOME_REQUEST)
            .`when`()
            .put("/hearing/{hearingId}/defendant/{defendantId}/outcome", HEARING_ID, DEFENDANT_ID)
            .then()
            .statusCode(200)

        val hearing = hearingNotesServiceInitService.initializeNote(HEARING_ID).get()
        val hearingOutcome = hearing.hearingDefendants[0].hearingOutcome
        assertThat(hearingOutcome.outcomeType).isEqualTo("ADJOURNED")
        assertThat(hearingOutcome.outcomeDate).isNotNull()
    }

    @Test
    fun `given hearing id and outcome recorded, should update with new hearing outcome`() {

        val hearingId = "ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"
        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(HEARING_OUTCOME_UPDATE_REQUEST)
            .`when`()
            .put("/hearing/{hearingId}/defendant/{defendantId}/outcome", hearingId, DEFENDANT_ID)
            .then()
            .statusCode(200)

        val hearing = hearingNotesServiceInitService.initializeNote(hearingId).get();
        val hearingOutcome = hearing.hearingDefendants[0].hearingOutcome
        assertThat(hearingOutcome.outcomeType).isEqualTo("REPORT_REQUESTED")
        assertThat(hearingOutcome.outcomeDate).isNotNull()
    }

    @Test
    fun `given court code and outcome state NEW and filters should return all outcomes for that court`() {

        val courtCode = "B33HU"

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
            .body(HEARING_OUTCOME_REQUEST)
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
            .body("courtRoomFilters", contains("2"))
            .body("countsByState.toResultCount", equalTo(1))
            .body("countsByState.inProgressCount", equalTo(0))
            .body("countsByState.resultedCount", equalTo(0))
            .body("totalPages", equalTo(1))
            .body("page", equalTo(1))
            .body("totalElements", equalTo(1))
    }

    @Test
    fun `given court code and outcome state IN_PROGRESS return all outcomes for that court`() {

        val courtCode = "B10JQ"

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
                .body("countsByState.toResultCount", equalTo(0))
                .body("countsByState.inProgressCount", equalTo(2))
                .body("countsByState.resultedCount", equalTo(0))
                .body("totalPages", equalTo(1))
                .body("page", equalTo(1))
                .body("totalElements", equalTo(2))
                .body("courtRoomFilters", contains("1", "2"))
    }

    @Test
    fun `given court code and outcome state IN_PROGRESS and assinged to user, should return outcomes corectly`() {

        val courtCode = "B10JQ"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
                .queryParam("state", "IN_PROGRESS")
                .queryParam("assignedToUuid", listOf("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
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
                .body("cases[0].state", equalTo("IN_PROGRESS"))
                .body("countsByState.toResultCount", equalTo(0))
                .body("countsByState.inProgressCount", equalTo(2))
                .body("countsByState.resultedCount", equalTo(0))
                .body("totalPages", equalTo(1))
                .body("page", equalTo(1))
                .body("totalElements", equalTo(1))
    }

    @Test
    fun `given court code and assigned to uuid then return all outcomes assigned to that user id`() {

        val courtCode = "B10JQ"

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
            .body("cases[0].crn", equalTo("X320741"))
            .body("cases[0].probationStatus", equalTo("Current"))
            .body("cases[0].assignedTo", equalTo("Joe Blogs"))
            .body("cases[0].assignedToUuid", equalTo("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
    }

    @Test
    fun `given court code and outcome state NEW and filters do not match should return empty response`() {

        val courtCode = "B33HU"

        val endpoint = UriComponentsBuilder.fromUri(URI("/courts/${courtCode}/hearing-outcomes"))
            .queryParam("state", "NEW")
            .queryParam("outcomeType",  "PROBATION_SENTENCE", "REPORT_REQUESTED")
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
            .body("cases", hasSize<Any>(0))
    }

    @Test
    fun `given a known hearing id and assign to string should update hearing outcome and return a 200 response `() {
        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(HEARING_OUTCOME_ASSIGN_REQUEST)
                .`when`()
                .put("/hearing/{hearingId}/defendant/{defendantId}/outcome/assign", HEARING_ID, DEFENDANT_ID)
                .then()
                .statusCode(200)

        val hearing = hearingNotesServiceInitService.initializeNote(HEARING_ID).get()
        val hearingOutcome = hearing.hearingDefendants[0].hearingOutcome
        assertThat(hearingOutcome.assignedTo).isEqualTo("John Smith")
        assertThat(hearingOutcome.assignedToUuid).isEqualTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81")
    }

    @Test
    fun `given an unknown hearing id and assign to string should return a 404 response `() {
        given()
                .auth()
                .oauth2(TokenHelper.getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(HEARING_OUTCOME_ASSIGN_REQUEST)
                .`when`()
                .put("/hearing/{hearingId}/defendant/{defendantId}/outcome/assign", UNKNOWN_HEARING_ID, DEFENDANT_ID)
                .then()
                .statusCode(404)

    }

    @Test
    fun `given hearing id with outcome in IN_PROGRESS state, result the case`() {

        val hearingId = "ddfe6b75-c3fc-4ed0-9bf6-21d66b125636"
        given()
            .auth()
            .oauth2(TokenHelper.getToken("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .post("/hearing/{hearingId}/defendant/{defendantId}/outcome/result", hearingId, DEFENDANT_ID)
            .then()
            .statusCode(200)

        val hearing = hearingNotesServiceInitService.initializeNote(hearingId).get();
        val hearingOutcome = hearing.hearingDefendants[0].hearingOutcome
        assertThat(hearingOutcome.state).isEqualTo("RESULTED")
        assertThat(hearingOutcome.resultedDate).isNotNull()
    }

    @Test fun `should trigger move un resulted cases to outcomes workflow`() {
        given()
            .`when`()
            .put("/process-un-resulted-cases")
            .then()
            .statusCode(200)

        verify(caseWorkflowService).processUnResultedCases()
    }

    @Test fun `return a list of hearing outcome types`() {
        val expectedResult = HearingOutcomeType.entries.toTypedArray()
        given()
            .auth()
            .oauth2(TokenHelper.getToken("4b03d065-4c96-4b24-8d6d-75a45d2e3f12"))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .get("/hearing-outcome-types")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(expectedResult.size))
            .body("get(0).value", equalTo("PROBATION_SENTENCE"))
            .body("get(0).label", equalTo("Probation sentence"))
            .body("get(6).value", equalTo("NO_OUTCOME"))
            .body("get(6).label", equalTo("No outcome"))
            .body("get(9).value", equalTo("TRIAL"))
            .body("get(9).label", equalTo("Trial"))
    }

    @Test
    fun `given hearing, defendant and prep status, should update prep status`() {

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .`when`()
            .put("/hearing/{hearingId}/defendants/{defendantId}/prep-status/IN_PROGRESS", HEARING_ID, DEFENDANT_ID)
            .then()
            .statusCode(200)

        val hearingPrepStatus = hearingNotesServiceInitService.initializeNote(HEARING_ID).get().getHearingDefendant(DEFENDANT_ID).prepStatus
        assertThat(hearingPrepStatus).isEqualTo("IN_PROGRESS")
    }

    @Test
    fun `given hearing, invalid defendant id and prep status, should throw entity not found`() {

        given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .`when`()
            .put("/hearing/{hearingId}/defendants/{defendantId}/prep-status/IN_PROGRESS", HEARING_ID, "invalid-defendant-id")
            .then()
            .statusCode(404)
    }
}