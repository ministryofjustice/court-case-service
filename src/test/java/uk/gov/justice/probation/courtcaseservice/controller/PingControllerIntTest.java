package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.configureRestAssuredForIntTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
public class PingControllerIntTest {

    @LocalServerPort
    int port;

    @Before
    public void before() {
        configureRestAssuredForIntTest(port);
    }

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