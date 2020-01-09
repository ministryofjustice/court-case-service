package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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
    private final String CASE_NO = "1600028913";
    private final String NEW_CASE_NO = "1700028914";
    private final Long COURT_ID = 4444443L;
    private final LocalDateTime now = LocalDateTime.now();
    private final CourtCaseEntity caseDetails = new CourtCaseEntity();

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));

        caseDetails.setCaseId(Long.parseLong(NEW_CASE_NO));
        caseDetails.setCaseNo(NEW_CASE_NO);
        caseDetails.setCourtId(COURT_ID);
        caseDetails.setCourtRoom("1");
        caseDetails.setSessionStartTime(now);
        caseDetails.setData("{}");
    }

    @Test
    public void shouldGetCaseWhenCourtExists() throws JsonProcessingException {
        given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() throws JsonProcessingException {
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

        String NOT_FOUND_COURT_CODE = "LPL";

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

        caseDetails.setCourtId(0L);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_NO)
                .then()
                .statusCode(500)
                .body(containsString("constraint [fk_court_case_court]"));
    }

    @Test
    public void createCaseData() {
        CourtCaseEntity result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_NO)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtCaseEntity.class);

        assertThat(result.getCaseId()).isEqualTo(Long.parseLong(NEW_CASE_NO));
        assertThat(result.getCaseNo()).isEqualTo(NEW_CASE_NO);
        assertThat(result.getCourtId()).isEqualTo(COURT_ID);
        assertThat(result.getCourtRoom()).isEqualTo("1");
        assertThat(result.getSessionStartTime()).isEqualTo(now.toString());
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
                .put("/case/" + NEW_CASE_NO)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtCaseEntity.class);

        assertThat(newResult.getCaseId()).isEqualTo(Long.parseLong(NEW_CASE_NO));
        assertThat(newResult.getCaseNo()).isEqualTo(NEW_CASE_NO);
        assertThat(newResult.getCourtId()).isEqualTo(COURT_ID);
        assertThat(newResult.getCourtRoom()).isEqualTo("2");
        assertThat(newResult.getSessionStartTime()).isEqualTo(now.toString());
    }
}
