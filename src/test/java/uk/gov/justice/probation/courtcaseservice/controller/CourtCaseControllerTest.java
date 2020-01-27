package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
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
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerTest {

    /* before-test.sql sets up a court case in the database */

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private final String COURT_CODE = "SHF";
    private final String CASE_ID = "123456";
    private final String NEW_CASE_ID = "654321";
    private final String CASE_NO = "1600028913";
    private final String NEW_CASE_NO = "1700028914";
    private final String PROBATION_STATUS = "No record";
    private final String NOT_FOUND_COURT_CODE = "LPL";
    private final LocalDateTime now = LocalDateTime.now();
    private final CourtCaseEntity caseDetails = new CourtCaseEntity();

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));

        caseDetails.setCaseId(NEW_CASE_ID);
        caseDetails.setCaseNo(NEW_CASE_NO);
        caseDetails.setCourtCode(COURT_CODE);
        caseDetails.setCourtRoom("1");
        caseDetails.setSessionStartTime(now);
        caseDetails.setProbationStatus(PROBATION_STATUS);
        caseDetails.setData("{}");
    }

    @Test
    public void cases_shouldGetCaseListWhenCasesExist() {

        CaseListResponse result = when()
                .get("/court/{courtCode}/cases?date={date}", "SHF", LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .body()
                .as(CaseListResponse.class);

        assertThat(result.getCases().size()).isEqualTo(3);
        assertThat(result.getCases().get(0).getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(result.getCases().get(0).getCaseId()).isEqualTo("5555555");

        assertThat(result.getCases().get(0).getSessionStartTime()).isEqualTo(LocalDateTime.of(2019, 12, 14, 9, 0));
        assertThat(result.getCases().get(1).getSessionStartTime()).isEqualTo(LocalDateTime.of(2019, 12, 14, 0, 0));
        assertThat(result.getCases().get(2).getSessionStartTime()).isEqualTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59));
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
        CourtCaseEntity result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtCaseEntity.class);

        assertThat(result.getCaseNo()).isEqualTo(CASE_NO);
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
        InputMismatchException result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + mismatchCaseId)
                .then()
                .statusCode(500)
                .extract()
                .body()
                .as(InputMismatchException.class);

        assertThat(result.getMessage()).contains("Case ID " + mismatchCaseId + " does not match with " + NEW_CASE_ID);
    }

    @Test
    public void createCaseDataWithDuplicateCaseNo() {

        String newCaseId = "666666";
        caseDetails.setCaseId(newCaseId);
        caseDetails.setCaseNo(CASE_NO);

        InputMismatchException result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + newCaseId)
                .then()
                .statusCode(500)
                .extract()
                .body()
                .as(InputMismatchException.class);

        assertThat(result.getMessage()).contains("constraint [court_case_case_no_idempotent]");
    }

    @Test
    public void createCaseData() {
        CourtCaseEntity result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtCaseEntity.class);

        assertThat(result.getCaseId()).isEqualTo(NEW_CASE_ID);
        assertThat(result.getCaseNo()).isEqualTo(NEW_CASE_NO);
        assertThat(result.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(result.getCourtRoom()).isEqualTo("1");
        assertThat(result.getSessionStartTime()).isEqualTo(now.toString());
        assertThat(result.getProbationStatus()).isEqualTo(PROBATION_STATUS);
    }

    @Test
    public void updateCaseData() {

        createCaseData();

        caseDetails.setCourtRoom("2");

        CourtCaseEntity newResult = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtCaseEntity.class);

        assertThat(newResult.getCaseId()).isEqualTo(NEW_CASE_ID);
        assertThat(newResult.getCaseNo()).isEqualTo(NEW_CASE_NO);
        assertThat(newResult.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(newResult.getCourtRoom()).isEqualTo("2");
        assertThat(newResult.getProbationStatus()).isEqualTo(PROBATION_STATUS);
        assertThat(newResult.getSessionStartTime()).isEqualTo(now.toString());
    }
}
