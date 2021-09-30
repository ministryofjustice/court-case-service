package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_ROOM;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest extends BaseIntTest {

    public static final String KEY_ID = "mock-key";
    private static final String LAST_MODIFIED_COURT_CODE = "B14LO";
    private static final String DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002";

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    @Autowired
    CourtCaseService courtCaseService;

    private static final LocalDate DECEMBER_14 = LocalDate.of(2019, Month.DECEMBER, 14);
    private static final LocalDateTime DECEMBER_14_9AM = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0);
    private static final String CASE_NO = "1600028913";
    private static final String PROBATION_STATUS = "Possible NDelius record";
    private static final String NOT_FOUND_COURT_CODE = "LPL";

    @Nested
    class GetCasesCaseNo {

        @BeforeEach
        void beforeEach() {
            ReflectionTestUtils.setField(courtCaseService, "caseListExtended", false);
        }

        @Test
        void GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, DECEMBER_14.format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(7))
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].defendantName", equalTo("Ms Emma Radical"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].probationStatus", equalToIgnoringCase("CURRENT"))
                .body("cases[0].hearings", hasSize(2))
                .body("cases[0].offences", hasSize(2))
                .body("cases[0].offences[0].offenceTitle", equalTo("Emma stole 1st thing from a shop"))
                .body("cases[1].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].offences", hasSize(1))
                .body("cases[1].offences[0].offenceTitle", equalTo("Billy stole from a shop"))
                .body("cases[2].offences", hasSize(2))
                .body("cases[2].caseNo", equalTo("1600028913"))
                .body("cases[2].preSentenceActivity", equalTo(true))
                .body("cases[2].probationStatus", equalTo("Possible NDelius record"))
                .body("cases[2].probationStatusActual", equalTo(null))
                .body("cases[2].offences[0].sequenceNumber", equalTo(1))
                .body("cases[2].offences[1].sequenceNumber", equalTo(2))
                .body("cases[2].numberOfPossibleMatches", equalTo(3))
                .body("cases[2].sessionStartTime", equalTo(DECEMBER_14_9AM.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].awaitingPsr", equalTo(true))
                .body("cases[4].caseNo", equalTo("1600028916"))
                .body("cases[5].caseNo", equalTo("1600028915"))
                .body("cases[5].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[5].probationStatus", equalTo("No record"))
                .body("cases[5].probationStatusActual", equalTo("NO_RECORD"))
                .body("cases[6].caseNo", equalTo("1600028918"))
                .body("cases[6].createdToday", equalTo(false));
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
                .get("/court/{courtCode}/cases?date={date}&createdAfter=2020-10-01T16:59:58.999", COURT_CODE, DECEMBER_14.format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(6))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[1].caseNo", equalTo("1600028914"))
                .body("cases[2].caseNo", equalTo("1600028913"))
                .body("cases[3].caseNo", equalTo("1600028917"))
                .body("cases[4].caseNo", equalTo("1600028915"))
                .body("cases[5].caseNo", equalTo("1600028918"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate() {
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}&createdBefore=2020-10-05T00:00:00", COURT_CODE,
                    LocalDate.of(2020, 5, 1).format(DateTimeFormatter.ISO_DATE))
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
                    COURT_CODE, DECEMBER_14.format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(1))
                .body("cases[0].caseNo", equalTo("1600028916"))
            ;
        }

        @Test
        void GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates() {

            var futureDecember14 = LocalDate.of(2200, Month.DECEMBER, 14).format(DateTimeFormatter.ISO_DATE);

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/B30NY/cases?date={date}&createdAfter=2020-09-01T16:59:59&createdBefore=2020-09-01T17:00:00", futureDecember14)
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

            String startTime = DECEMBER_14_9AM.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            var response = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .statusCode(200);

            response.body("caseNo", equalTo(CASE_NO));
            validateResponse(response, startTime, "1f93aa0a-7e46-4885-a1cb-f25a4be33a00");
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
    class GetCaseByCaseId {

        @Test
        void givenKnownCaseId_whenGetCase_thenReturn() {

            var response = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/case/{caseId}", "1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
                .then()
                .statusCode(200);

            response.body("$", not(hasKey("caseNo")));

            validateResponse(response, DECEMBER_14_9AM.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "1f93aa0a-7e46-4885-a1cb-f25a4be33a00");
        }

        @Test
        void givenUnknownCaseId_whenGetCase_thenReturn404() {

            final var NOT_FOUND_CASE_ID = "1f93bbcc-7e46-4885-a1cb-f25a4be33a56";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/case/{caseId}", NOT_FOUND_CASE_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + NOT_FOUND_CASE_ID + " not found");
            assertThat(result.getUserMessage()).contains("Case " + NOT_FOUND_CASE_ID + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void shouldReturnNotFoundForDeletedCase() {

            final var DELETED_CASE_ID = "5555559";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/case/{caseId}", DELETED_CASE_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + DELETED_CASE_ID + " not found");
            assertThat(result.getUserMessage()).contains("Case " + DELETED_CASE_ID + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

    }

    @Nested
    class GetCaseByCaseIdAndDefendantId {
        private static final String PATH = "/case/{caseId}/defendant/{defendantId}";
        private static final String CASE_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        @Test
        void givenKnownCaseId_whenGetCase_thenReturn() {

            var response = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, CASE_ID, DEFENDANT_ID)
                .then()
                .statusCode(200);

            response.body("$", not(hasKey("caseNo")));

            response
                .body("caseId", equalTo(CASE_ID))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("probationStatusActual", equalTo(null))
                .body("previouslyKnownTerminationDate", equalTo(null))
                .body("suspendedSentenceOrder", equalTo(false))
                .body("breach", equalTo(false))
                .body("source", equalTo("COMMON_PLATFORM"))
                .body("preSentenceActivity", equalTo(false))
                .body("crn", equalTo(null))
                .body("pnc", equalTo("A/1234560BA"))
                .body("cro", equalTo("311462/13E"))
                .body("listNo", equalTo("3rd"))
                .body("courtCode", equalTo(COURT_CODE))
                .body("sessionStartTime", equalTo(DECEMBER_14_9AM.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .body("defendantName", equalTo("Mr Johnny BALL"))
                .body("defendantId", equalTo(DEFENDANT_ID))
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
        void givenUnknownCaseId_whenGetCase_thenReturn404() {

            final var NOT_FOUND_CASE_ID = "1f93bbcc-7e46-4885-a1cb-f25a4be33a56";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, NOT_FOUND_CASE_ID, DEFENDANT_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + NOT_FOUND_CASE_ID + " not found");
            assertThat(result.getUserMessage()).contains("Case " + NOT_FOUND_CASE_ID + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void givenUnknownDefendantId_whenGetCase_thenReturn404() {

            final var NOT_FOUND_DEFENDANT_ID = "2f934532-7e46-4885-a1cb-f25a4be33a56";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, CASE_ID, NOT_FOUND_DEFENDANT_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + CASE_ID + " not found for defendant " + NOT_FOUND_DEFENDANT_ID);
            assertThat(result.getUserMessage()).contains("Case " + CASE_ID + " not found for defendant " + NOT_FOUND_DEFENDANT_ID);
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void shouldReturnNotFoundForDeletedCase() {

            final var DELETED_CASE_ID = "5555559";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, DELETED_CASE_ID, DEFENDANT_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Case " + DELETED_CASE_ID + " not found");
            assertThat(result.getUserMessage()).contains("Case " + DELETED_CASE_ID + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

    }

    private void validateResponse(ValidatableResponse validatableResponse, String startTime, String caseId) {
        validatableResponse
            .body("caseId", equalTo(caseId))
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
            .body("defendantId", equalTo(DEFENDANT_ID))
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

    @Nested
    class GetCasesExtended {

        private static final String BASE_PATH_WITH_DATE = "/court/%s/cases?date=%s";

        @BeforeEach
        void beforeEach() {
            ReflectionTestUtils.setField(courtCaseService, "caseListExtended", true);
        }

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
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseNo", equalTo("1600028914"))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].defendantName", equalTo("Ms Emma Radical"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].probationStatus", equalToIgnoringCase("Current"))
                .body("cases[0].hearings", hasSize(2))
                .body("cases[0].offences", hasSize(2))
                .body("cases[0].offences[0].offenceTitle", equalTo("Emma stole 1st thing from a shop"))
                .body("cases[0].numberOfPossibleMatches", equalTo(2))
                .body("cases[1].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].offences", hasSize(1))
                .body("cases[1].offences[0].offenceTitle", equalTo("Billy stole from a shop"))
                .body("cases[1].hearings", hasSize(2))
                .body("cases[1].hearings[0].courtCode", equalTo(COURT_CODE))
                .body("cases[1].hearings[0].courtRoom", equalTo(COURT_ROOM))
                .body("cases[1].hearings[0].listNo", equalTo("3rd"))
                .body("cases[1].hearings[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[1].hearings[0].session", equalTo("MORNING"))
                .body("cases[2].offences", hasSize(2))
                .body("cases[2].caseNo", equalTo("1600028913"))
                .body("cases[2].preSentenceActivity", equalTo(true))
                .body("cases[2].probationStatus", equalTo("Possible NDelius record"))
                .body("cases[2].probationStatusActual", equalTo(null))
                .body("cases[2].offences[0].sequenceNumber", equalTo(1))
                .body("cases[2].offences[1].sequenceNumber", equalTo(2))
                .body("cases[2].numberOfPossibleMatches", equalTo(3))
                .body("cases[2].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(9, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].awaitingPsr", equalTo(true))
                .body("cases[4].caseNo", equalTo("1600028916"))
                .body("cases[5].caseNo", equalTo("1600028915"))
                .body("cases[5].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(23, 59, 59)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[5].probationStatus", equalTo("No record"))
                .body("cases[5].probationStatusActual", equalTo("NO_RECORD"))
                .body("cases[6].caseNo", equalTo("1600028918"))
                .body("cases[6].createdToday", equalTo(false));
        }

    }

}
