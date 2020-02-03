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
import uk.gov.justice.probation.courtcaseservice.fixtures.CourtFixtures;

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

    private String expectedJson;

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;
    private CourtFixtures fixtures;

    @Before
    public void setup() throws IOException {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));

        fixtures = new CourtFixtures();
    }

    @Test
    public void whenCourtCreated_thenReturnSuccess() {
        given().body(fixtures.putBodyRequestJson)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(201)
                .body(jsonEquals(fixtures.createdResponseJson));
    }

    @Test
    public void givenCourtCodeInPathAndBodyConflict_whenCourtCreated_thenReturnBadRequest() {

        given().body(fixtures.putBodyRequestJson)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/BAD")
                .then()
                .assertThat()
                .statusCode(400)
                .body(jsonEquals(fixtures.conflictResponseJson));
    }

}
