package uk.gov.justice.probation.courtcaseservice.health;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

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
        assertThatJson(response).node("components.offenderAssessments.status").isEqualTo("UP");
        assertThatJson(response).node("components.communityApi.status").isEqualTo("UP");
        assertThatJson(response).node("components.nomisAuth.status").isEqualTo("UP");
        assertThatJson(response).node("components.nomisAuth.status").isEqualTo("UP");
        assertThatJson(response).node("components.hmpps-domain-events-health.status").isEqualTo("UP");
    }
}
