package uk.gov.justice.probation.courtcaseservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = { "classpath:sql/before-common.sql", "classpath:sql/before-search-tests.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CaseSearchControllerIntTest extends BaseIntTest {

    private static final String CASE_SEARCH_ENDPOINT = "/search?term={search}&type={type}";
    private static final String CASE_SEARCH_ENDPOINT_WITH_PAGING = "/search?term={search}&type={type}&page={page}&size={size}";

    @Autowired
    private EntityManager em;

    @Test
    void givenNameForSearchParameter_shouldReturnCasesHavingDefendantsWithGivenName() {
        var testName = "Ferris Bueller";
        RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get(CASE_SEARCH_ENDPOINT, testName, "NAME")

            .then()
            .statusCode(200)
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(1))
            .body("items", hasSize(1))
            .body("items[0].hearingId", equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
            .body("items[0].defendantId", equalTo("0048297a-fd9c-4c96-8c03-8122b802a54d"))
            .body("items[0].defendantName", equalTo("Mr Ferris Middle BUELLER"))
            .body("items[0].crn", equalTo("X258291"))
            .body("items[0].probationStatus", equalTo("Current"))
            .body("items[0].offenceTitles", equalTo(List.of("Theft from a garage")))
            .body("items[0].lastHearingDate", equalTo(LocalDate.now().minusDays(5).format(DateTimeFormatter.ISO_DATE)))
            .body("items[0].lastHearingCourt", equalTo("Leicester"))
            .body("items[0].awaitingPsr", equalTo(true))
            .body("items[0].breach", equalTo(true));
    }

    @Test
    void givenNameAndPagingInfo_shouldReturnCasesHavingDefendantsWithGivenName_forPageOne() {
        var testName = "Middle";
        RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get(CASE_SEARCH_ENDPOINT_WITH_PAGING, testName, "NAME", 1, 2)

            .then()
            .statusCode(200)
            .body("totalPages", equalTo(2))
            .body("totalElements", equalTo(3))
            .body("items", hasSize(2))
        ;
    }

    @Test
    void givenNameAndPagingInfo_shouldReturnCasesHavingDefendantsWithGivenName_forPageTwo() {
        var testName = "Middle";
        RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get(CASE_SEARCH_ENDPOINT_WITH_PAGING, testName, "NAME", 2, 2)

            .then()
            .statusCode(200)
            .body("totalPages", equalTo(2))
            .body("totalElements", equalTo(3))
            .body("items", hasSize(1))
        ;
    }

    @Test
    void givenCrn_shouldReturnCasesHavingDefendantsWithGivenCrn_usingCommonSearchTerm() {
        var testCrn = "X258291";
        ValidatableResponse validvalidatableResponse = RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get(CASE_SEARCH_ENDPOINT, testCrn, "CRN")

            .then();

        validvalidatableResponse
            .statusCode(200)
            .body("items", hasSize(2))

            .body("items[0].hearingId", equalTo("440dd779-8b0e-4012-90d5-2e2ee1189cd1"))
            .body("items[0].defendantId", equalTo("8acf5a7a-0e0b-49e5-941e-943ab354a15f"))
            .body("items[0].defendantName", equalTo("Mr Ferris Middle Biller"))
            .body("items[0].crn", equalTo(testCrn))
            .body("items[0].probationStatus", equalTo("Current"))
            .body("items[0].offenceTitles", equalTo(List.of("Theft from a hospital", "Theft from a shop")))
            .body("items[0].lastHearingDate", equalTo(LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_DATE)))
            .body("items[0].lastHearingCourt", equalTo("Sheffield"))
            .body("items[0].nextHearingDate", equalTo(LocalDate.now().plusDays(10).format(DateTimeFormatter.ISO_DATE)))
            .body("items[0].nextHearingCourt", equalTo("Leicester"))
            .body("items[0].awaitingPsr", equalTo(true))
            .body("items[0].breach", equalTo(true))

            .body("items[1].hearingId", equalTo("fe657c3a-b674-4e17-8772-7281c99e4f9f"))
            .body("items[1].defendantId", equalTo("0048297a-fd9c-4c96-8c03-8122b802a54d"))
            .body("items[1].defendantName", equalTo("Mr Ferris Middle BUELLER"))
            .body("items[1].crn", equalTo(testCrn))
            .body("items[1].probationStatus", equalTo("Current"))
            .body("items[1].offenceTitles", equalTo(List.of("Theft from a garage")))
            .body("items[1].lastHearingDate", equalTo(LocalDate.now().minusDays(5).format(DateTimeFormatter.ISO_DATE)))
            .body("items[1].lastHearingCourt", equalTo("Leicester"))
            .body("items[1].awaitingPsr", equalTo(true))
            .body("items[1].breach", equalTo(true))
        ;
    }
    @Test
    void givenInvalidSearchType_shouldRejectWithBadRequest() {
        var testCrn = "X258291";
        RestAssured.given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .when()
            .get(CASE_SEARCH_ENDPOINT, testCrn, "INVALIDTYPE")
            .then()
            .statusCode(400);
    }

    @Test
    void shouldPopulateTsVectorForDefendantNameCorrectly() {
        var q = em.createNativeQuery("select defendant_name, text(tsv_name) from defendant d where d.defendant_id = :did");
        String defendantId = "0048297a-fd9c-4c96-8c03-8122b802a54d";
        q.setParameter("did", defendantId);
        var res = q.getResultList();
        assertThat(Arrays.asList(res.get(0))).isEqualTo(List.of("Mr Ferris Middle BUELLER", "'bueller':4 'ferris':2 'middle':3 'mr':1"));
    }
}