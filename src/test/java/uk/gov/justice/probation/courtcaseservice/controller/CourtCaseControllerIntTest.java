package uk.gov.justice.probation.courtcaseservice.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest extends uk.gov.justice.probation.courtcaseservice.BaseIntTest {
    public static final String KEY_ID = "mock-key";

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String CASE_NO = "1600028913";
    private static final String PROBATION_STATUS = "Previously known";
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    public void GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases() {

        given()
                .auth()
                .oauth2(getToken())
        .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(6))
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[0].caseId", equalTo("5555556"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].lastUpdated", containsString(now.format(DateTimeFormatter.ISO_DATE)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[1].offences", hasSize(2))
                .body("cases[1].caseNo", equalTo("1600028913"))
                .body("cases[1].offences[0].sequenceNumber", equalTo(1))
                .body("cases[1].offences[1].sequenceNumber", equalTo(2))
                .body("cases[1].numberOfPossibleMatches", equalTo(3))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].caseNo", equalTo("1600028917"))
                .body("cases[2].lastUpdated", equalTo(LocalDateTime.of(2020, 10, 1, 18, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[3].caseNo", equalTo("1600028916"))
                .body("cases[4].caseNo", equalTo("1600028915"))
                .body("cases[4].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[5].caseNo", equalTo("1600028918"))
                .body("cases[5].createdToday", equalTo(false));
    }

    @Test
    public void GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime() {

        given()
                .auth()
                .oauth2(getToken())
        .when()
                .get("/court/{courtCode}/cases?date={date}&createdAfter=2020-10-01T16:59:58.999", COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(5))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[1].caseNo", equalTo("1600028913"))
                .body("cases[2].caseNo", equalTo("1600028917"))
                .body("cases[3].caseNo", equalTo("1600028915"))
                .body("cases[4].caseNo", equalTo("1600028918"))
        ;
    }

    @Test
    public void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
        given()
                .auth()
                .oauth2(getToken())
        .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, "2020-02-02")
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", empty());

    }

    @Test
    public void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
        given()
                .auth()
                .oauth2(getToken())
        .when()
                .get("/court/{courtCode}/cases", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(400)
                .body("developerMessage", equalTo("Required LocalDate parameter 'date' is not present"));
    }

    @Test
    public void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
        ErrorResponse result = given()
                .auth()
                .oauth2(getToken())
        .when()
                .get("/court/{courtCode}/cases?date={date}", NOT_FOUND_COURT_CODE, "2020-02-02")
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldGetCaseWhenCourtExists() {
        given()
                .given()
                .auth()
                .oauth2(getToken())
        .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() {
        String startTime = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0, 0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        given()
                .given()
                .auth()
                .oauth2(getToken())
        .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .statusCode(200)
                .body("caseNo", equalTo(CASE_NO))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("crn", equalTo("X320741"))
                .body("cro", equalTo("311462/13E"))
                .body("pnc", equalTo("A/1234560BA"))
                .body("listNo", equalTo("3rd"))
                .body("courtCode", equalTo(COURT_CODE))
                .body("sessionStartTime", equalTo(startTime))
                .body("lastUpdated", containsString(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))) // TODO: Update this test to match created time
                .body("defendantName", equalTo("Mr Johnny BALL"))
                .body("name.title", equalTo("Mr"))
                .body("name.forename1", equalTo("Johnny"))
                .body("name.forename2", equalTo("John"))
                .body("name.forename3", equalTo("Jon"))
                .body("name.surname", equalTo("BALL"))
                .body("defendantAddress.line1", equalTo("27"))
                .body("defendantAddress.line2", equalTo("Elm Place"))
                .body("defendantAddress.postcode", equalTo("ad21 5dr"))
                .body("defendantAddress.line3", equalTo("Bangor"))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("defendantDob", equalTo(LocalDate.of(1958, Month.OCTOBER, 10).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("defendantSex", equalTo("M"))
                .body("nationality1", equalTo("British"))
                .body("nationality2", equalTo("Polish"))
                .body("removed", equalTo(false))
                .body("createdToday", equalTo(true))
                .body("numberOfPossibleMatches", equalTo(3))
        ;
    }


    @Test
    public void shouldReturnNotFoundForNonexistentCase() {

        String NOT_FOUND_CASE_NO = "11111111111";

        ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
        .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, NOT_FOUND_CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getUserMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentCourt() {
        ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
        .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", NOT_FOUND_COURT_CODE, CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

}
