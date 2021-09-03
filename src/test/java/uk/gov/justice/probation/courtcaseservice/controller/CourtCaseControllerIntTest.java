package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest extends BaseIntTest {

    public static final String KEY_ID = "mock-key";
    private static final String LAST_MODIFIED_COURT_CODE = "B14LO";

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String CASE_NO = "1600028913";
    private static final String PROBATION_STATUS = "Possible NDelius record";
    private static final String NOT_FOUND_COURT_CODE = "LPL";

    @Nested
    class GetCasesCaseNo {

        @Test
        void GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases() {

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
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].probationStatus", equalTo("Pre-sentence record"))
                .body("cases[0].probationStatusActual", equalTo("NOT_SENTENCED"))
                .body("cases[1].offences", hasSize(2))
                .body("cases[1].caseNo", equalTo("1600028913"))
                .body("cases[1].preSentenceActivity", equalTo(true))
                .body("cases[1].probationStatus", equalTo("Possible NDelius record"))
                .body("cases[1].probationStatusActual", equalTo(null))
                .body("cases[1].offences[0].sequenceNumber", equalTo(1))
                .body("cases[1].offences[1].sequenceNumber", equalTo(2))
                .body("cases[1].numberOfPossibleMatches", equalTo(3))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[1].awaitingPsr", equalTo(true))
                .body("cases[3].caseNo", equalTo("1600028916"))
                .body("cases[4].caseNo", equalTo("1600028915"))
                .body("cases[4].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[4].probationStatus", equalTo("No record"))
                .body("cases[4].probationStatusActual", equalTo("NO_RECORD"))
                .body("cases[5].caseNo", equalTo("1600028918"))
                .body("cases[5].createdToday", equalTo(false));
        }

        @Test
        void givenLastModifiedRecent_whenRequestCases_thenReturnLastModifiedHeader() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}", LAST_MODIFIED_COURT_CODE, LocalDate.of(2021, 6, 1).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .header("Last-Modified", equalTo("Tue, 01 Jun 2021 16:59:59 GMT"))
                .header("Cache-Control", equalTo("max-age=1"))
            ;
        }

        @Test
        void givenNoDataChange_whenGetCases_thenReturn304() {

            given()
                .auth()
                .oauth2(getToken())
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, "Tue, 04 Feb 1970 19:57:25 GMT")
                .when()
                .get("/court/{courtCode}/cases?date={date}", LAST_MODIFIED_COURT_CODE, LocalDate.of(2021, 6, 1).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(304)
                .header("Cache-Control", equalTo("max-age=1"))
            ;
        }

        @Test
        void GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}&createdAfter=2020-10-01T16:59:58.999", COURT_CODE,
                    LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
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
        void GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate() {
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}&createdBefore=2020-10-05T00:00:00", COURT_CODE,
                    LocalDate.of(2020, 5, 01).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028930"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_whenGetCases_thenReturnCasesBetweenSpecifiedTimes() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}&createdAfter=2020-09-01T16:59:59&createdBefore=2020-09-01T17:00:00",
                    COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028916"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/B30NY/cases?date={date}&createdAfter=2020-09-01T16:59:59&createdBefore=2020-09-01T17:00:00",
                    LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028919"))
                .body("cases[0].defendantName", equalTo("Hubert Farnsworth"))
            ;
        }

        @Test
        void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
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
        void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(400)
                .body("developerMessage", equalTo("Required request parameter 'date' for method parameter type LocalDate is not present"));
        }

        @Test
        void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
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
    }

    @Nested
    class GetCaseByCourtAndCaseNo {

        @Test
        void shouldGetCaseWhenCourtExists() {
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
        void shouldGetCaseWhenExists() {

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
                .body("probationStatusActual", equalTo(null))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("source", equalTo("COMMON_PLATFORM"))
                .body("preSentenceActivity", equalTo(true))
                .body("crn", equalTo(null))
                .body("pnc", equalTo("A/1234560BA"))
                .body("cro", equalTo("311462/13E"))
                .body("listNo", equalTo("3rd"))
                .body("courtCode", equalTo(COURT_CODE))
                .body("sessionStartTime", equalTo(startTime))
                .body("defendantName", equalTo("Mr Johnny BALL"))
                .body("defendantId", equalTo("40db17d6-04db-11ec-b2d8-0242ac130002"))
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
        void shouldReturnNotFoundForNonexistentCase() {

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
        void shouldReturnNotFoundForDeletedCase() {

            String DELETED_CASE_NO = "1600128918";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, DELETED_CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + DELETED_CASE_NO + " not found");
            assertThat(result.getUserMessage()).contains("Case " + DELETED_CASE_NO + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void shouldReturnNotFoundForNonexistentCourt() {
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

    @Nested
    class GetCasesExtended {

        private static final String BASE_PATH_WITH_DATE = "/court/%s/cases/extended?date=%s";
        private static final String BASE_PATH = "/court/%s/cases/extended";

        @Test
        void GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases() {

            final var path = String.format(BASE_PATH_WITH_DATE, COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE));
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get(path)
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(6))
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].source", equalTo("COMMON_PLATFORM"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].probationStatus", equalTo("Pre-sentence record"))
                .body("cases[0].probationStatusActual", equalTo("NOT_SENTENCED"))
                .body("cases[1].offences", hasSize(2))
                .body("cases[1].caseNo", equalTo("1600028913"))
                .body("cases[1].preSentenceActivity", equalTo(true))
                .body("cases[1].probationStatus", equalTo("Possible NDelius record"))
                .body("cases[1].probationStatusActual", equalTo(null))
                .body("cases[1].offences[0].sequenceNumber", equalTo(1))
                .body("cases[1].offences[1].sequenceNumber", equalTo(2))
                .body("cases[1].numberOfPossibleMatches", equalTo(3))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[1].awaitingPsr", equalTo(true))
                .body("cases[3].caseNo", equalTo("1600028916"))
                .body("cases[4].caseNo", equalTo("1600028915"))
                .body("cases[4].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[4].probationStatus", equalTo("No record"))
                .body("cases[4].probationStatusActual", equalTo("NO_RECORD"))
                .body("cases[5].caseNo", equalTo("1600028918"))
                .body("cases[5].createdToday", equalTo(false));
        }

        @Test
        void givenLastModifiedRecent_whenRequestCases_thenReturnLastModifiedHeader() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases/extended?date={date}", LAST_MODIFIED_COURT_CODE, LocalDate.of(2021, 6, 1).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .header("Last-Modified", equalTo("Tue, 01 Jun 2021 16:59:59 GMT"))
                .header("Cache-Control", equalTo("max-age=1"))
            ;
        }

        @Test
        void givenNoDataChange_whenGetCases_thenReturn304() {

            given()
                .auth()
                .oauth2(getToken())
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, "Tue, 04 Feb 1970 19:57:25 GMT")
                .when()
                .get("/court/{courtCode}/cases/extended?date={date}", LAST_MODIFIED_COURT_CODE, LocalDate.of(2021, 6, 1).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(304)
                .header("Cache-Control", equalTo("max-age=1"))
            ;
        }

        @Test
        void GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases/extended?date={date}&createdAfter=2020-10-01T16:59:58.999", COURT_CODE,
                    LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
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
        void GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate() {
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases/extended?date={date}&createdBefore=2020-10-05T00:00:00", COURT_CODE,
                    LocalDate.of(2020, 5, 01).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028930"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_whenGetCases_thenReturnCasesBetweenSpecifiedTimes() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases/extended?date={date}&createdAfter=2020-09-01T16:59:59&createdBefore=2020-09-01T17:00:00",
                    COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028916"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/B30NY/cases/extended?date={date}&createdAfter=2020-09-01T16:59:59&createdBefore=2020-09-01T17:00:00",
                    LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028919"))
                .body("cases[0].defendantName", equalTo("Hubert Farnsworth"))
            ;
        }

        @Test
        void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
            final var path = String.format(BASE_PATH_WITH_DATE, COURT_CODE, "2020-02-02");
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get(path)
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", empty());
        }

        @Test
        void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
            final var path = String.format(BASE_PATH, COURT_CODE);
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get(path)
                .then()
                .assertThat()
                .statusCode(400)
                .body("developerMessage", equalTo("Required request parameter 'date' for method parameter type LocalDate is not present"));
        }

        @Test
        void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
            final var path = String.format(BASE_PATH_WITH_DATE, NOT_FOUND_COURT_CODE, "2020-02-02");
            ErrorResponse result = given()
                .auth()
                .oauth2(getToken())
                .when()
                .get(path)
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
    }

}
