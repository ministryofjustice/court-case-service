package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest {

    /* before-test.sql sets up a court case in the database */

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String CRN = "X320741";
    private static final String CASE_ID = "123456";
    private final String COURT_CODE = "SHF";
    private final String NEW_CASE_ID = "654321";
    private final String CASE_NO = "1600028913";
    private final String NEW_CASE_NO = "1700028914";
    private final String PROBATION_STATUS = "Previously known";
    private final String NOT_FOUND_COURT_CODE = "LPL";
    private final String DEFENDANT_NAME = "JTEST";
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14,9, 0);
    private final CourtCaseEntity caseDetails = new CourtCaseEntity();
    private String caseDetailsJson;

    @Before
    public void setup() throws IOException {
        caseDetailsJson = getFileAsString("integration/request/PUT_courtCase_success.json");
        TestConfig.configureRestAssuredForIntTest(port);

        var addressProperty = new AddressPropertiesEntity("27", "Elm Place", "ad21 5dr", "Bangor", null, null);

        caseDetails.setCaseId(NEW_CASE_ID);
        caseDetails.setCaseNo(NEW_CASE_NO);
        caseDetails.setCourtCode(COURT_CODE);
        caseDetails.setCourtRoom("1");
        caseDetails.setSessionStartTime(now);
        caseDetails.setProbationStatus(PROBATION_STATUS);
        caseDetails.setData("{}");
        caseDetails.setLastUpdated(now);
        caseDetails.setPreviouslyKnownTerminationDate(LocalDate.of(2010,1,1));
        caseDetails.setSuspendedSentenceOrder(true);
        caseDetails.setBreach(true);
        caseDetails.setDefendantName(DEFENDANT_NAME);
        caseDetails.setDefendantAddress(addressProperty);
    }

    @Test
    public void cases_shouldGetCaseListWhenCasesExist() {

        when()
                .get("/court/{courtCode}/cases?date={date}", "SHF", LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseId", equalTo("5555555"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].offences", hasSize(2))
                .body("cases[1].lastUpdated", containsString(now.format(DateTimeFormatter.ISO_DATE)))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    @Test
    public void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
        when()
                .get("/court/{courtCode}/cases?date={date}", "SHF", "2020-02-02")
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", empty());

    }

    @Test
    public void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
        when()
                .get("/court/{courtCode}/cases", "SHF")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Required LocalDate parameter 'date' is not present"));
    }

    @Test
    public void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
        ErrorResponse result = when()
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
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() {
        given()
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
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo("27"))
                .body("defendantAddress.line2", equalTo("Elm Place"))
                .body("defendantAddress.postcode", equalTo("ad21 5dr"))
                .body("defendantAddress.line3", equalTo("Bangor"))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null));





    }


    @Test
    public void shouldReturnNotFoundForNonexistentCase() {

        String NOT_FOUND_CASE_NO = "11111111111";

        ErrorResponse result = given()
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

    @Test
    public void createCaseDataWithIncorrectCourt() {

        caseDetails.setCourtCode(NOT_FOUND_COURT_CODE);

        ErrorResponse result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + CASE_ID)
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
    public void createCaseDataWithCaseIdMismatch() {
        String mismatchCaseId = "000000";
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + mismatchCaseId)
                .then()
                .statusCode(500)
                .body("message", equalTo("Case ID " + mismatchCaseId + " does not match with " + NEW_CASE_ID));
    }

    @Test
    public void createCaseDataWithDuplicateCaseNo() {

        String newCaseId = "666666";
        caseDetails.setCaseId(newCaseId);
        caseDetails.setCaseNo(CASE_NO);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + newCaseId)
                .then()
                .statusCode(500)
                .body("message", containsString("constraint [court_case_case_no_idempotent]"));
    }

    @Test
    public void createCaseData() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsJson)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo(NEW_CASE_ID))
                .body("caseNo", equalTo(NEW_CASE_NO))
                .body("crn", equalTo(CRN))
                .body("courtCode", equalTo(COURT_CODE))
                .body("courtRoom", equalTo("1"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo("27"))
                .body("defendantAddress.line2", equalTo("Elm Place"))
                .body("defendantAddress.postcode", equalTo("ad21 5dr"))
                .body("defendantAddress.line3", equalTo("Bangor"))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
        ;

    }

    @Test
    public void updateCaseData() {

        createCaseData();

        String updatedJson = caseDetailsJson.replace("\"courtRoom\": \"1\"", "\"courtRoom\": \"2\"");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo(NEW_CASE_ID))
                .body("caseNo", equalTo(NEW_CASE_NO))
                .body("crn", equalTo(CRN))
                .body("courtCode", equalTo(COURT_CODE))
                .body("courtRoom", equalTo("2"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo("27"))
                .body("defendantAddress.line2", equalTo("Elm Place"))
                .body("defendantAddress.postcode", equalTo("ad21 5dr"))
                .body("defendantAddress.line3", equalTo("Bangor"))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"));
    }

    @Test
    public void whenCourtCaseCreated_thenOffenceSequenceNumberShouldBeReflectedInResponse() {
        var modifiedJson = caseDetailsJson
                .replace("\"sequenceNumber\": 1", "\"sequenceNumber\": 4")
                .replace("\"sequenceNumber\": 2", "\"sequenceNumber\": 3");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(modifiedJson)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(201)
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a different shop"))
                .body("offences[1].offenceTitle", equalTo("Theft from a shop"));

    }

    @SuppressWarnings("UnstableApiUsage")
    private String getFileAsString(String resourcePath) throws IOException {
        return Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
    }
}
