package uk.gov.justice.probation.courtcaseservice.smoke;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
public class CommunityApiSmokeTest {

    @Value("${smoke-test.target-host:http://localhost:8080}")
    private String host;

    @Before
    public void setup() {
        TestConfig.configureRestAssuredForSmokeTest(host);
    }

    @Test
    public void valueCanBeRetrievedFromCommunityApi() {
        when()
                .get("/test/community-api-get-something")
                .then()
                .assertThat()
                .statusCode(200)
                .body("content[0].code", equalTo("A00"))
                .body("content[0].description", equalTo("Transfer Provider"));
    }
}