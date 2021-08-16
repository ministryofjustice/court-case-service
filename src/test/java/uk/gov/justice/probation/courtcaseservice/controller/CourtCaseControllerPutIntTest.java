package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_SEX;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.LIST_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_1;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_2;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aCourtCaseEntity;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtCaseControllerPutIntTest extends BaseIntTest {

    /* before-test.sql sets up a court case in the database */

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String PUT_CASE_BY_CASENO_PATH = "/court/%s/case/%s";
    private static final String CRN = "X320741";
    private static final String PNC = "A/1234560BA";
    private static final String COURT_ROOM = "1";
    private static final AddressPropertiesEntity ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "Bangor", null, null, "ad21 5dr");
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private static final String DEFENDANT_NAME = "Mr Dylan Adam ARMSTRONG";
    private static final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14, 9, 0);

    @Value("classpath:integration/request/PUT_courtCase_success.json")
    private Resource caseDetailsResource;

    @Value("classpath:integration/request/PUT_courtCaseExtended_success.json")
    private Resource caseDetailsExtendedResource;

    private String caseDetailsJson;
    private String caseDetailsExtendedJson;

    /** NEW values match those from JSON file incoming. */
    private static final String JSON_CASE_NO = "1700028914";
    private static final String JSON_CASE_ID = "654321";

    @BeforeEach
    void beforeEach() throws Exception {
        caseDetailsJson = Files.readString(caseDetailsResource.getFile().toPath());
        caseDetailsExtendedJson = Files.readString(caseDetailsExtendedResource.getFile().toPath());
        super.setup();
    }

    @Nested
    class PutByCourtAndCaseNo {

        @Test
        void whenCreateCaseDataByCourtAndCaseNo_ThenCreateNewRecord() {

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsJson)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, JSON_CASE_NO))
                .then()
                .statusCode(201)
                .body("caseNo", equalTo(JSON_CASE_NO))
                .body("crn", equalTo(CRN))
                .body("courtCode", equalTo(COURT_CODE))
                .body("courtRoom", equalTo(COURT_ROOM))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("preSentenceActivity", equalTo(true))
                .body("breach", equalTo(true))
                .body("defendantType", equalTo("PERSON"))
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo(ADDRESS.getLine1()))
                .body("defendantAddress.line2", equalTo(ADDRESS.getLine2()))
                .body("defendantAddress.postcode", equalTo(ADDRESS.getPostcode()))
                .body("defendantAddress.line3", equalTo(ADDRESS.getLine3()))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[0]", not(hasKey("courtCase")))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
                .body("pnc", equalTo(PNC))
                .body("listNo", equalTo(LIST_NO))
                .body("defendantDob", equalTo(LocalDate.of(1958, 12, 14).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("defendantSex", equalTo(DEFENDANT_SEX))
                .body("nationality1", equalTo(NATIONALITY_1))
                .body("nationality2", equalTo(NATIONALITY_2))
                .body("awaitingPsr", equalTo(true))
            ;

        }

        @Test
        void whenUpdateCaseDataByCourtAndCaseNo_ThenUpdate() {

            createCase();

            final var updatedJson = caseDetailsJson.replace("\"courtRoom\": \"1\"", "\"courtRoom\": \"2\"");

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, JSON_CASE_NO))
                .then()
                .statusCode(201)
                .body("caseNo", equalTo(JSON_CASE_NO))
                .body("courtCode", equalTo(COURT_CODE))
                .body("crn", equalTo(CRN))
                .body("pnc", equalTo(PNC))
                .body("listNo", equalTo(LIST_NO))
                .body("defendantDob", equalTo(LocalDate.of(1958, 12, 14).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("defendantSex", equalTo(DEFENDANT_SEX))
                .body("nationality1", equalTo(NATIONALITY_1))
                .body("nationality2", equalTo(NATIONALITY_2))
                .body("courtRoom", equalTo("2"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("preSentenceActivity", equalTo(true))
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo(ADDRESS.getLine1()))
                .body("defendantAddress.line2", equalTo(ADDRESS.getLine2()))
                .body("defendantAddress.line3", equalTo(ADDRESS.getLine3()))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("defendantAddress.postcode", equalTo(ADDRESS.getPostcode()))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
            ;

        }

        @Test
        void whenUpdateCaseDataByCourtAndCaseNo_ThenUpdateProbationStatusOnCurrentCasesWithSameCrn() {

            var existingCaseNo = "15000";
            var newCaseNo = "15005";
            var crn = "X320747";
            var updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"" + newCaseNo + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                ;

            final var preUpdateCourtCase = courtCaseRepository.findByCourtCodeAndCaseNo("B10JQ", existingCaseNo);
            assertThat(preUpdateCourtCase)
                .map(CourtCaseEntity::getProbationStatus)
                .hasValue("No record");

            // Create
            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, newCaseNo))
                .then()
                .statusCode(201)
                .body("caseNo", equalTo(newCaseNo))
                .body("crn", equalTo(crn))
                .body("probationStatus", equalTo("Previously known"))
            ;

            final var courtCaseEntity = courtCaseRepository.findByCourtCodeAndCaseNo("B10JQ", existingCaseNo);
            assertThat(courtCaseEntity)
                .map(CourtCaseEntity::getProbationStatus)
                .hasValue("PREVIOUSLY_KNOWN");
        }

        @Test
        void givenNullCrn_whenUpdateCaseDataByCourtAndCaseNo_ThenDoNotUpdateOtherStatuses() {

            var newCaseNo = "15005";
            var updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"" + newCaseNo + "\"")
                .replace("\"crn\": \"X320741\",", "")
                ;

            // Create
            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, newCaseNo))
                .then()
                .statusCode(201)
                .body("caseNo", equalTo(newCaseNo))
                .body("probationStatus", equalTo("Previously known"))
            ;
        }

        @Test
        void whenCreateCourtCaseByCourtAndCaseWithUnknownCourt_ThenRaise404() {

            final var courtCaseEntity = createCaseDetails(NOT_FOUND_COURT_CODE);

            ErrorResponse result = given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(courtCaseEntity)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, NOT_FOUND_COURT_CODE, JSON_CASE_NO))
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
            assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void whenCreateCourtCaseByCourtAndCaseWithMismatchCourt_ThenRaise400() {

            final var courtCaseEntity = createCaseDetails(COURT_CODE);

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(courtCaseEntity)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, "NWS", "99999"))
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Case No 99999 and Court Code NWS do not match with values from body 1700028914 and B10JQ"))
            ;
        }
    }

    @Nested
    class OffenderMatches {

        @Test
        void whenUpdateCaseDataByCourtAndCaseNo_ThenUpdateOffenderMatchesConfirmedRejectedFlags() {

            final var updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"1600028913\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"2234\"")
                .replace("\"pnc\": \"A/1234560BA\"", "\"pnc\": \"223456\"")
                .replace("\"cro\": \"99999\"", "\"cro\": \"22345\"");

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, "1600028913"))
                .then()
                .statusCode(201)
                .body("caseNo", equalTo("1600028913"))
                .body("crn", equalTo("2234"))
                .body("pnc", equalTo("223456"))
            ;

            given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/court/" + COURT_CODE + "/case/1600028913/grouped-offender-matches/9999991")
                .then()
                .statusCode(200)
                .body("offenderMatches", hasSize(3))
                .body("offenderMatches[1].crn", equalTo("X320741"))
                .body("offenderMatches[1].confirmed", equalTo(false))
                .body("offenderMatches[1].rejected", equalTo(true))
            ;

            given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/court/" + COURT_CODE + "/case/1600028914/grouped-offender-matches/9999992")
                .then()
                .statusCode(200)
                .body("offenderMatches[0].crn", equalTo("3234"))
                .body("offenderMatches[0].confirmed", equalTo(false))
                .body("offenderMatches[0].rejected", equalTo(true))
            ;
        }
    }

    @Nested
    class PutByCaseIdExtended {

        private static final String JSON_CASE_ID = "ac24a1be-939b-49a4-a524-21a3d228f8bc";

        @Test
        void whenCreateCaseExtendedByCaseId_thenCreateNewRecord() {

            final var caseWithSameCrn = courtCaseRepository.findByCaseIdOrderByCreatedDesc("1000000")
                .stream()
                .findFirst();
            assertThat(caseWithSameCrn)
                .map(CourtCaseEntity::getProbationStatus)
                .hasValue("No record");

            courtCaseRepository.findFirstByCaseIdOrderByIdDesc("1000000")
                .map(aCase -> assertThat(aCase.getProbationStatus()).isEqualTo("No record"));

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(String.format("/case/%s/extended", JSON_CASE_ID))
                .then()
                .statusCode(201)
                .body("caseId", equalTo(JSON_CASE_ID))
            ;

            final var otherCaseWithSameCrn = courtCaseRepository.findByCaseIdOrderByCreatedDesc("1000000")
                .stream()
                .findFirst();
            assertThat(otherCaseWithSameCrn)
                .map(CourtCaseEntity::getProbationStatus)
                .hasValue("PREVIOUSLY_KNOWN");
        }

        @Test
        void givenUnknownCourt_whenCreateCourtCaseByCaseId_ThenRaise404() {
            var updatedJson = caseDetailsExtendedJson
                .replace("\"courtCode\": \"B14LO\"", "\"courtCode\": \"" + NOT_FOUND_COURT_CODE + "\"")
                ;

            ErrorResponse result = given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", JSON_CASE_ID))
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

            assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
            assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
            assertThat(result.getStatus()).isEqualTo(404);
        }

        @Test
        void givenMismatchCaseId_whenCreateCourtCase_ThenRaise400() {

            final var ALTERNATIVE_CASE_ID = "6eb32e9f-ae7a-4d13-a026-b4a6f8e8731a";
            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(String.format("/case/%s/extended", ALTERNATIVE_CASE_ID))
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Case Id " + ALTERNATIVE_CASE_ID + " does not match with value from body " + JSON_CASE_ID))
            ;
        }
    }

    private CourtCaseEntity createCaseDetails(String courtCode) {
        return aCourtCaseEntity(null, JSON_CASE_NO, LocalDateTime.now(), PROBATION_STATUS, JSON_CASE_ID, courtCode);
    }

    private void createCase() {

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseDetailsJson)
            .when()
            .put(String.format(PUT_CASE_BY_CASENO_PATH, COURT_CODE, JSON_CASE_NO))
            .then()
            .statusCode(201)
            .body("caseNo", equalTo(JSON_CASE_NO))
            .body("crn", equalTo(CRN))
            .body("courtCode", equalTo(COURT_CODE))
        ;
    }


}
