package uk.gov.justice.probation.courtcaseservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = { "classpath:sql/before-common.sql", "classpath:sql/before-search-tests.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CaseSearchControllerIntTest extends BaseIntTest {

    @Test
    void givenCrn_shouldReturnCasesHavingDefendantsWithGivenCrn() {
        RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get("/search?crn={crn}", "X25829")

            .then()
            .statusCode(200)
            .body("items", hasSize(2))
            .body("items[0].hearingId", equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
            .body("items[0].defendantId", equalTo("0048297a-fd9c-4c96-8c03-8122b802a54d"))
            .body("items[0].defendantName", equalTo("Mr Ferris BUELLER"))
            .body("items[0].crn", equalTo("X25829"))
            .body("items[0].probationStatus", equalTo("Current"))
            .body("items[0].offenceTitles", equalTo(List.of("Theft from a garage")))
            .body("items[0].lastHearingDate", equalTo(LocalDate.now().minusDays(5).format(DateTimeFormatter.ISO_DATE)))
            .body("items[0].lastHearingCourt", equalTo("Leicester"))
            .body("items[0].awaitingPsr", equalTo(true))
            .body("items[0].breach", equalTo(true))
            .body("items[1].hearingId", equalTo("440dd779-8b0e-4012-90d5-2e2ee1189cd1"))
            .body("items[1].defendantId", equalTo("8acf5a7a-0e0b-49e5-941e-943ab354a15f"))
            .body("items[1].defendantName", equalTo("Mr Ferris Biller"))
            .body("items[1].crn", equalTo("X25829"))
            .body("items[1].probationStatus", equalTo("Current"))
            .body("items[1].offenceTitles", equalTo(List.of("Theft from a hospital", "Theft from a shop")))
            .body("items[1].lastHearingDate", equalTo(LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_DATE)))
            .body("items[1].lastHearingCourt", equalTo("Sheffield"))
            .body("items[1].nextHearingDate", equalTo(LocalDate.now().plusDays(10).format(DateTimeFormatter.ISO_DATE)))
            .body("items[1].nextHearingCourt", equalTo("Leicester"))
            .body("items[1].awaitingPsr", equalTo(true))
            .body("items[1].breach", equalTo(true));
    }
}