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
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommunityApiTestControllerIntTest {

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        TestConfig.configureRestAssuredForIntTest(port);
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