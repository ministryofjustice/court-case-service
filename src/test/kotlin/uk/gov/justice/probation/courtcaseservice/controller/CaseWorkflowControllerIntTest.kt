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
import org.testcontainers.shaded.org.hamcrest.collection.IsCollectionWithSize
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
}