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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtControllerTest {


    private static final String COURT_CODE = "FOO";
    private static final String PUT_BODY =
            "{" +
                    "\"name\": \"Sheffield Magistrates Court\"," +
                    "\"courtCode\": \"" + COURT_CODE + "\"" +
                    "}";
    public static final String PUT_COURT_201_RESPONSE = "src/test/resources/controller/PUT_court_201_response.json";
    public static final String PUT_COURT_400_RESPONSE = "src/test/resources/controller/PUT_court_400_response.json";

    private String expectedJson;

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

        given().body(PUT_BODY)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(201)
                .body("courtCode", equalTo(COURT_CODE))
                .body("name", equalTo("Sheffield Magistrates Court"))
                .body(jsonEquals(getJson(PUT_COURT_201_RESPONSE)));
    }

    @Test
    public void givenCourtCodeInPathAndBodyConflict_whenCourtCreated_thenReturnBadRequest() throws IOException {
        given().body(PUT_BODY)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/BAD")
                .then()
                .assertThat()
                .statusCode(400)
                .body(jsonEquals(getJson(PUT_COURT_400_RESPONSE)));
    }

    private String getJson(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return String.join("\n", Files.readAllLines(path));
    }
}
