package uk.gov.justice.probation.courtcaseservice.health;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.configureRestAssuredForIntTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
@DirtiesContext
public class HealthCheckIntTest {

    @LocalServerPort
    int port;

    @Before
    public void before() {
        configureRestAssuredForIntTest(port);
    }

    @Test
    public void testUp() {

        String response = given()
                .when()
                .get("/health/")
                .then()
                .statusCode(200)
                .extract().response().asString();

        assertThatJson(response).node("status").isEqualTo("UP");
        assertThatJson(response).node("components.db.details.database").isEqualTo("PostgreSQL");
    }
}
