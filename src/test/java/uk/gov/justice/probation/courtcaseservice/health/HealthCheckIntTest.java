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
                .get("/health")
                .then()
                .statusCode(200)
                .extract().response().asString();

        assertThatJson(response).node("status").isEqualTo("UP");
        assertThatJson(response).node("components.db.details.database").isEqualTo("PostgreSQL");
        assertThatJson(response).node("components.offenderAssessments.status").isEqualTo("UP");
        assertThatJson(response).node("components.communityApi.status").isEqualTo("UP");
        assertThatJson(response).node("components.offenceApi.status").isEqualTo("UP");
        assertThatJson(response).node("components.nomisAuth.status").isEqualTo("UP");
        assertThatJson(response).node("components.hmppsdomainevents-health.status").isEqualTo("UP");
    }

    @Test
    public void testDLQRetryEndpointIsUnsecured() {
        // Access to this endpoint is restricted to within the namespace in helm_deploy/court-case-service/templates/ingress.yaml

        given()
                .when()
                .put("/queue-admin/retry-all-dlqs")
                .then()
                .statusCode(200)
                .extract().response().asString();
    }
    @Test
    public void testProcessUnresultedIsUnSecured() {
        // Access to this endpoint is restricted to within the namespace in helm_deploy/court-case-service/templates/ingress.yaml

        given()
                .when()
                .put("/process-un-resulted-cases")
                .then()
                .statusCode(200)
                .extract().response().asString();
    }
}
