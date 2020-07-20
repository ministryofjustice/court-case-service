package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.ClassRule;
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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtControllerIntTest {


    private static final String COURT_CODE = "FOO";
    private static final String PUT_BODY =
            "{" +
                    "\"name\": \"Sheffield Magistrates Court\"," +
                    "\"courtCode\": \"" + COURT_CODE + "\"" +
                    "}";

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .port(WIREMOCK_PORT)
            .usingFilesUnderClasspath("mocks"));

    @Before
    public void setup() {
        TestConfig.configureRestAssuredForIntTest(port);
    }

    @Test
    public void whenCourtCreated_thenReturnSuccess() {

        CourtEntity result = given()
                .auth()
                .oauth2(getToken())
                .body(PUT_BODY)
            .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/{courtCode}", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .body()
                .as(CourtEntity.class);

        assertThat(result.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(result.getName()).isEqualTo("Sheffield Magistrates Court");
    }

    @Test
    public void givenCourtCodeInPathAndBodyConflict_whenCourtCreated_thenReturnBadRequest() {
        given()
                .auth()
                .oauth2(getToken()).body(PUT_BODY)
            .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/BAD")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
