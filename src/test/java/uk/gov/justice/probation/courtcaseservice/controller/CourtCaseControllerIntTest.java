package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
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
    HearingRepository courtCaseRepository;

    @Autowired
    CourtCaseService courtCaseService;

    private static final LocalDate DECEMBER_14 = LocalDate.of(2019, Month.DECEMBER, 14);
    private static final LocalDate JAN_1_2010 = LocalDate.of(2010, 1, 1);
    private static final LocalDateTime DECEMBER_14_9AM = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0);
    private static final String CASE_NO = "1600028913";
    private static final String NOT_FOUND_COURT_CODE = "LPL";

    @Nested
    class GetCasesCaseNo {
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
                .body("cases[0].caseNo", equalTo(null))
                .body("cases[0].source", equalTo("COMMON_PLATFORM"))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].defendantName", equalTo("Ms Emma Radical"))
                .body("cases[0].phoneNumber.mobile", equalTo("07000000006"))
                .body("cases[0].phoneNumber.home", equalTo("07000000013"))
                .body("cases[0].phoneNumber.work", equalTo("07000000015"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].probationStatus", equalToIgnoringCase("Possible NDelius record"))
                .body("cases[0].hearings", hasSize(2))
                .body("cases[0].offences", hasSize(2))
                .body("cases[0].offences[0].offenceTitle", equalTo("Emma stole 1st thing from a shop"))
                .body("cases[0].offences[0].listNo", equalTo(35))
                .body("cases[1].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].offences", hasSize(1))
                .body("cases[1].offences[0].offenceTitle", equalTo("Billy stole from a shop"))
                .body("cases[2].offences", hasSize(2))
                .body("cases[2].caseNo", equalTo("1600028913"))
                .body("cases[2].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("cases[2].source", equalTo("LIBRA"))
                .body("cases[2].probationStatus", equalToIgnoringCase("Current"))
                .body("cases[2].offences[0].sequenceNumber", equalTo(1))
                .body("cases[2].offences[1].sequenceNumber", equalTo(2))
                .body("cases[2].numberOfPossibleMatches", equalTo(3))
                .body("cases[2].sessionStartTime", equalTo(DECEMBER_14_9AM.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[4].caseNo", equalTo(null))
                .body("cases[4].source", equalTo("COMMON_PLATFORM"))
                .body("cases[5].caseNo", equalTo(null))
                .body("cases[5].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a57"))
                .body("cases[5].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a57"))
                .body("cases[5].source", equalTo("COMMON_PLATFORM"))
                .body("cases[5].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[5].probationStatus", equalTo("Pre-sentence record"))
                .body("cases[5].probationStatusActual", equalTo("NOT_SENTENCED"))
                .body("cases[6].caseNo", equalTo(null))
                .body("cases[6].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a18"))
                .body("cases[6].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a18"))
                .body("cases[6].createdToday", equalTo(false))
            ;
        }

        @Test
        void GET_cases_givenDefendantIsConfirmedAsNoMatch_should_return_probation_status_as_No_Record() {

            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, DECEMBER_14.format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(7))
                .body("cases[0].defendantId", equalTo("7a320a46-037c-481c-ab1e-dbfab62af4d6"))
                .body("cases[0].probationStatus", equalToIgnoringCase("Possible NDelius record"))
            ;
             given()
                .auth()
                .oauth2(getToken())
                .when()
                .delete("/defendant/7a320a46-037c-481c-ab1e-dbfab62af4d6/offender")
                .then()
                .assertThat()
                .statusCode(200)
            ;

             // assert case list
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, DECEMBER_14.format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", hasSize(7))
                .body("cases[0].defendantId", equalTo("7a320a46-037c-481c-ab1e-dbfab62af4d6"))
                .body("cases[0].probationStatus", equalToIgnoringCase("No record"))
            ;

            // assert defendant endpoint
            given()
                .auth()
                .oauth2(getToken())
                .when()
                .get("/hearing/1f93aa0a-7e46-4885-a1cb-f25a4be33a56/defendant/7a320a46-037c-481c-ab1e-dbfab62af4d6")
                .then()
                .assertThat()
                .statusCode(200)
                .body("defendantId", equalTo("7a320a46-037c-481c-ab1e-dbfab62af4d6"))
                .body("probationStatus", equalToIgnoringCase("No record"))
            ;
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
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[2].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("cases[2].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("cases[3].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a59"))
                .body("cases[3].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a59"))
                .body("cases[4].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a57"))
                .body("cases[4].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a57"))
                .body("cases[5].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a18"))
                .body("cases[5].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a18"))
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
                .body("cases[0].caseNo", equalTo(null))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a30"))
                .body("cases[0].source", equalTo("COMMON_PLATFORM"))
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
                .body("cases[0].caseNo", equalTo(null))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a58"))
                .body("cases[0].defendantId", equalTo("44817de0-cc89-460a-8f07-0b06ef45982a"))
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
        void shouldReturnOkForNonexistentCourt() {
            given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", NOT_FOUND_COURT_CODE, CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body();
        }
    }

    @Nested
    class GetCaseByHearingIdAndDefendantId {
        private static final String PATH = "/hearing/{hearingId}/defendant/{defendantId}";
        private static final String CASE_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
        private static final String HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        @Test
        void givenKnownCaseIdWithNoOffender_whenGetCase_thenReturn() {

            var response = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, HEARING_ID, DEFENDANT_ID)
                .then()
                .statusCode(200);

            response
                .body("caseId", equalTo(CASE_ID))
                .body("hearingId", equalTo(HEARING_ID))
                .body("urn", equalTo("URN008"))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
                .body("probationStatus", equalTo("Current"))
                .body("probationStatusActual", equalTo("CURRENT"))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, Month.JANUARY, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("preSentenceActivity", equalTo(true))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("source", equalTo("LIBRA"))
                .body("caseNo", equalTo("1600028913"))
                .body("crn", equalTo("X320741"))
                .body("pnc", equalTo("A/1234560BA"))
                .body("cro", equalTo("311462/13E"))
                .body("listNo", equalTo("3rd"))
                .body("courtCode", equalTo(COURT_CODE))
                .body("sessionStartTime", equalTo(DECEMBER_14_9AM.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .body("defendantName", equalTo("Mr Johnny BALL"))
                .body("defendantId", equalTo(DEFENDANT_ID))
                .body("phoneNumber.mobile", equalTo("07000000007"))
                .body("phoneNumber.home", equalTo("07000000013"))
                .body("phoneNumber.work", equalTo("07000000015"))
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
                .body("caseComments", hasSize(2))
                .body("caseComments[0].commentId", notNullValue())
                .body("caseComments[0].comment", equalTo("PSR in progress"))
                .body("caseComments[0].createdBy", equalTo("before-test.sql"))
                .body("caseComments[1].commentId", notNullValue())
                .body("caseComments[1].comment", equalTo("PSR completed"))
                .body("caseComments[1].createdBy", equalTo("before-test.sql"))
            ;
        }

        @Test
        void givenKnownHearingIdWithOffender_whenGetHearing_thenReturn() {

            var hearingId = "683bcde4-611f-4487-9833-f68090507b74";
            var defendantId = "005ae89b-46e9-4fa5-bb5e-d117011cab32";
            var response = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, hearingId, defendantId)
                .then()
                .statusCode(200);

            response
                .body("caseId", equalTo("683bcde4-611f-4487-9833-f68090507b74"))
                .body("hearingId", equalTo(hearingId))
                .body("defendantId", equalTo(defendantId))
                .body("phoneNumber.mobile", equalTo("07000000008"))
                .body("phoneNumber.home", equalTo("07000000013"))
                .body("phoneNumber.work", equalTo("07000000015"))
                .body("probationStatus", equalToIgnoringCase("Previously known"))
                .body("probationStatusActual", equalTo("PREVIOUSLY_KNOWN"))
                .body("crn", equalTo("C16000"))
                .body("breach", equalTo(true))
                .body("preSentenceActivity", equalTo(true))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("previouslyKnownTerminationDate", equalTo(JAN_1_2010.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            ;
        }

        @Test
        void givenUnknownHearingId_whenGetCase_thenReturn404() {

            final var NOT_FOUND_HEARING_ID = "1f93bbcc-7e46-4885-a1cb-f25a4be33a56";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, NOT_FOUND_HEARING_ID, DEFENDANT_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Hearing " + NOT_FOUND_HEARING_ID + " not found");
            assertThat(result.getUserMessage()).contains("Hearing " + NOT_FOUND_HEARING_ID + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void givenUnknownDefendantId_whenGetHearingByDefendant_thenReturn404() {

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

            assertThat(result.getDeveloperMessage()).contains("Hearing " + HEARING_ID + " not found for defendant " + NOT_FOUND_DEFENDANT_ID);
            assertThat(result.getUserMessage()).contains("Hearing " + HEARING_ID + " not found for defendant " + NOT_FOUND_DEFENDANT_ID);
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void shouldReturnNotFoundForDeletedHearing() {

            final var DELETED_HEARING_ID = "1b6cf731-1892-4b9e-abc3-7fab87a39c21";

            ErrorResponse result = given()
                .given()
                .auth()
                .oauth2(getToken())
                .when()
                .header("Accept", "application/json")
                .get(PATH, DELETED_HEARING_ID, DEFENDANT_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Hearing 1b6cf731-1892-4b9e-abc3-7fab87a39c21 not found for defendant 40db17d6-04db-11ec-b2d8-0242ac130002");
            assertThat(result.getUserMessage()).contains("Hearing 1b6cf731-1892-4b9e-abc3-7fab87a39c21 not found for defendant 40db17d6-04db-11ec-b2d8-0242ac130002");
            assertThat(result.getStatus()).isEqualTo(404);
        }

    }

    private void validateResponse(ValidatableResponse validatableResponse, String startTime, String caseId) {
        validatableResponse
            .body("caseNo", equalTo(CASE_NO))
            .body("caseId", equalTo(caseId))
            .body("offences", hasSize(2))
            .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
            .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
            .body("probationStatus", equalTo("Current"))
            .body("probationStatusActual", equalTo("CURRENT"))
            .body("previouslyKnownTerminationDate", equalTo(JAN_1_2010.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("suspendedSentenceOrder", equalTo(true))
            .body("breach", equalTo(true))
            .body("preSentenceActivity", equalTo(true))
            .body("source", equalTo("LIBRA"))
            .body("pnc", equalTo("A/1234560BA"))
            .body("cro", equalTo("311462/13E"))
            .body("listNo", equalTo("3rd"))
            .body("courtCode", equalTo(COURT_CODE))
            .body("sessionStartTime", equalTo(startTime))
            .body("defendantName", equalTo("Mr Johnny BALL"))
            .body("defendantId", equalTo(DEFENDANT_ID))
            .body("phoneNumber.mobile", equalTo("07000000007"))
            .body("phoneNumber.home", equalTo("07000000013"))
            .body("phoneNumber.work", equalTo("07000000015"))
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
                .body("cases.size()", equalTo(7))
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseNo", equalTo(null))
                .body("cases[0].source", equalTo("COMMON_PLATFORM"))
                .body("cases[0].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[0].courtRoom", equalTo(COURT_ROOM))
                .body("cases[0].defendantType", equalTo("PERSON"))
                .body("cases[0].defendantName", equalTo("Ms Emma Radical"))
                .body("cases[0].phoneNumber.mobile", equalTo("07000000006"))
                .body("cases[0].phoneNumber.home", equalTo("07000000013"))
                .body("cases[0].phoneNumber.work", equalTo("07000000015"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].createdToday", equalTo(true))
                .body("cases[0].crn", equalTo(null))
                .body("cases[0].probationStatus", equalToIgnoringCase("Possible NDelius record"))
                .body("cases[0].hearings", hasSize(2))
                .body("cases[0].offences", hasSize(2))
                .body("cases[0].offences[0].offenceTitle", equalTo("Emma stole 1st thing from a shop"))
                .body("cases[0].numberOfPossibleMatches", equalTo(2))
                .body("cases[0].phoneNumber.mobile", equalTo("07000000006"))
                .body("cases[0].phoneNumber.home", equalTo("07000000013"))
                .body("cases[0].phoneNumber.work", equalTo("07000000015"))
                .body("cases[1].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a56"))
                .body("cases[1].offences", hasSize(1))
                .body("cases[1].offences[0].offenceTitle", equalTo("Billy stole from a shop"))
                .body("cases[1].hearings", hasSize(2))
                .body("cases[1].hearings[0].courtCode", equalTo(COURT_CODE))
                .body("cases[1].hearings[0].courtRoom", equalTo(COURT_ROOM))
                .body("cases[1].hearings[0].listNo", equalTo("3rd"))
                .body("cases[1].hearings[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(7, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[1].hearings[0].session", equalTo("MORNING"))
                .body("cases[2].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("cases[2].offences", hasSize(2))
                .body("cases[2].caseNo", equalTo("1600028913"))
                .body("cases[2].source", equalTo("LIBRA"))
                .body("cases[2].probationStatus", equalToIgnoringCase("Current"))
                .body("cases[2].numberOfPossibleMatches", equalTo(3))
                .body("cases[2].offences[0].sequenceNumber", equalTo(1))
                .body("cases[2].offences[1].sequenceNumber", equalTo(2))
                .body("cases[2].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(9, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[3].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a59"))
                .body("cases[4].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a58"))
                .body("cases[4].caseNo", equalTo(null))
                .body("cases[4].source", equalTo("COMMON_PLATFORM"))
                .body("cases[5].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a57"))
                .body("cases[5].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(23, 59, 59)).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[5].probationStatus", equalTo("Pre-sentence record"))
                .body("cases[5].probationStatusActual", equalTo("NOT_SENTENCED"))
                .body("cases[6].caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a18"))
                .body("cases[6].createdToday", equalTo(false))
            ;
        }
    }

    @Nested
    class GetCaseByHearingId {

        @Test
        void givenExistingHearingId_whenGetCaseByHearingId_thenReturnIntTest() {

            given()
                .auth()
                .oauth2(getToken())
            .when()
                .header("Accept", "application/json")
                .get("/hearing/{hearingId}", "1f93aa0a-7e46-4885-a1cb-f25a4be33a00")
            .then()
                .statusCode(200)
                .body("source", equalTo("LIBRA"))
                .body("caseNo", equalTo("1600028913"))
                .body("caseId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("urn", equalTo("URN008"))
                .body("hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
                .body("defendants", hasSize(1))
                .body("defendants[0].type", equalTo("PERSON"))
                .body("defendants[0].phoneNumber.mobile", equalTo("07000000007"))
                .body("defendants[0].phoneNumber.home", equalTo("07000000013"))
                .body("defendants[0].phoneNumber.work", equalTo("07000000015"))
                .body("defendants[0].name.title", equalTo("Mr"))
                .body("defendants[0].name.forename1", equalTo("Johnny"))
                .body("defendants[0].name.surname", equalTo("BALL"))
                .body("defendants[0].offences", hasSize(2))
                .body("defendants[0].offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("defendants[0].offences[0].listNo", equalTo(10))
                .body("defendants[0].offender.pnc", equalTo("PNCINT007"))
                .body("hearingDays", hasSize(1))
                .body("hearingDays[0].courtCode", equalTo(COURT_CODE))
                .body("hearingDays[0].courtRoom", equalTo(COURT_ROOM))
                .body("hearingDays[0].listNo", equalTo("3rd"))
                .body("hearingDays[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(9, 0)).format(DateTimeFormatter.ISO_DATE_TIME)));
        }
    }
}
