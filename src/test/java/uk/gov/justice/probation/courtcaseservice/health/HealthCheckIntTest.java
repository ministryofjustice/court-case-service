package uk.gov.justice.probation.courtcaseservice.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


@RunWith(SpringRunner.class)
@DirtiesContext
public class HealthCheckIntTest extends BaseIntTest {

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
        assertThatJson(response).node("components.offenderAssessmentsPing.status").isEqualTo("UP");
        assertThatJson(response).node("components.communityApiPing.status").isEqualTo("UP");
    }
}
