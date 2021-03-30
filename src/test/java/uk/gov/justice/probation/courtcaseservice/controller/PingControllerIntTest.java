package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


class PingControllerIntTest extends BaseIntTest {

    @Test
    void pingEndpoint() {

        String response = given()
                .when()
                .get("/ping")
                .then()
                .statusCode(200)
                .extract().response().asString();

        assertThat(response).isEqualTo("pong");
    }
}
