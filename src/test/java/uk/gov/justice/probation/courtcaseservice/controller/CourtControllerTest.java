package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
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
import uk.gov.justice.probation.courtcaseservice.fixtures.Fixtures;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtControllerTest {
    private static final String COURT_CODE = "FOO";

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));

    }

    @Test
    public void whenCourtCreated_thenReturnSuccess() throws IOException {
        given().body(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_request.json"))
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED_201)
                .body(jsonEquals(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_201_response.json")));
    }

    @Test
    public void givenCourtCodeInPathAndBodyDontMatch_whenCourtCreated_thenReturnBadRequest() throws IOException {

        given().body(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_request.json"))
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/BAD")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(jsonEquals(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_400_response.json")));
    }

    @Test
    public void givenCourtAlreadyExists_whenCourtCreated_thenReturnConflict() throws IOException {


        given() // court already exists
                .body(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_request.json"))
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE);

        given().body(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_request.json"))
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.CONFLICT_409)
                .body(jsonEquals(Fixtures.getJson("src/test/resources/fixtures/court/PUT_court_409_response.json")));
    }

}
