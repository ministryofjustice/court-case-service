package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtControllerIntTest extends BaseIntTest {


    private static final String COURT_CODE = "FOO";
    private static final String PUT_BODY =
            "{" +
                    "\"name\": \"Sheffield Magistrates Court\"," +
                    "\"courtCode\": \"" + COURT_CODE + "\"" +
                    "}";

    @Autowired
    ObjectMapper mapper;

    @Test
    void whenCourtCreated_thenReturnSuccess() {

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
    void givenCourtCodeInPathAndBodyConflict_whenCourtCreated_thenReturnBadRequest() {
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

    @Test
    void whenGetCourts_thenReturnListSorted() {

        given()
            .auth()
            .oauth2(getToken())
            .when()
            .get("/courts")
            .then()
            .assertThat()
            .statusCode(200)
            .body("courts", hasSize(6))
            .body("courts[0].code", equalTo("B63AD"))
            .body("courts[0].name", equalTo("Aberystwyth"))
            .body("courts[1].code", equalTo("B33HU"))
            .body("courts[1].name", equalTo("Leicester"))
            .body("courts[2].code", equalTo("B30NY"))
            .body("courts[2].name", equalTo("New New York"))
            .body("courts[3].code", equalTo("B10JQ"))
            .body("courts[3].name", equalTo("North Shields"))
            .body("courts[4].code", equalTo("C10JQ"))
            .body("courts[4].name", equalTo("Old New York"))
            .body("courts[5].code", equalTo("B14LO"))
            .body("courts[5].name", equalTo("Sheffield"))
        ;
    }
}
