package uk.gov.justice.probation.courtcaseservice.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.configureRestAssuredForIntTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
public class FeatureFlagIntTest {

    @LocalServerPort
    int port;

    @Before
    public void before() {
        configureRestAssuredForIntTest(port);
    }

    @Test
    public void toggleEndpoint() {

        given()
                .when()
                .get("/feature-flags")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("fetch-attendance-data", is(true))
                .body("disable-auth", is(true));
    }
}
