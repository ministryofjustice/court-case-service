package uk.gov.justice.probation.courtcaseservice.health;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.probation.courtcaseservice.test.LocalDbCreationRule;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ActiveProfiles("localDynamo")
public class HealthCheckTest {

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();

    @LocalServerPort
    int port;

    @Before
    public void before() {
        RestAssured.port = port;
        RestAssured.basePath = "/actuator";
    }

    @Test
    public void testUp() throws IOException {

        String response = given()
                .when()
                .get("health")
                .then()
                .statusCode(200)
                .extract().response().asString();

        assertThatJson(response).node("status").isEqualTo("UP");
    }
}
