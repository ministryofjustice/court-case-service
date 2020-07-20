package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
public class PingControllerIntTest extends BaseIntTest {

    @Test
    public void pingEndpoint() {

        String response = given()
                .when()
                .get("/ping")
                .then()
                .statusCode(200)
                .extract().response().asString();

        assertThat(response).isEqualTo("pong");
    }
}