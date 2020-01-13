package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
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

    /*
        before-test.sql sets up a court case in the database
     */

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;


    @Autowired
    CourtCaseRepository courtCaseRepository;

    private final String COURT_CODE = "SHF";
    private final String NOT_FOUND_COURT_CODE = "LPL";
    private final String NOT_FOUND_CASE_NO = "11111111111";
    private String CASE_NO = "1600028912";

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));

    }

    @Test
    public void shouldGetCaseWhenCourtExists() throws JsonProcessingException {
        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() throws JsonProcessingException {
        var result = given()
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
        var result = given()
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
        var result = given()
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


