package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommunityApiTestControllerTest {

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        TestConfig.configureRestAssuredForIntTest(port);
    }

    @Test
    public void tokenCanBeRetrievedFromNomisOauth() {
           when()
                   .get("/test/community-api-get-token")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body(containsString("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9")); // This is the JWT token header we're expecting
    }
}