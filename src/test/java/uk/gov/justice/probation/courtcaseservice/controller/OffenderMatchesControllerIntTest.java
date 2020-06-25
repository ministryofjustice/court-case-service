package uk.gov.justice.probation.courtcaseservice.controller;


import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.RetryService;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@RunWith(SpringRunner.class)
@EnableRetry
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderMatchesControllerIntTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RetryService retryService;

    @Before
    public void setUp() throws Exception {
        TestConfig.configureRestAssuredForIntTest(port);

        retryService.tryWireMockStub();
    }

    @ClassRule
    public static  final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
                                                                .port(WIREMOCK_PORT)
                                                                .usingFilesUnderClasspath("mocks"));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    @Test
    public void givenCaseExists_whenPostMadeToOffenderMatches_thenReturn201CreatedWithValidLocation() {
        String location = given()
                .accept(APPLICATION_JSON_VALUE)
                .body("{\n" +
                        "    \"matchIdentifiers\": {\n" +
                        "        \"crn\": \"X346204\",\n" +
                        "        \"pnc\": \"pnc123\",\n" +
                        "        \"cro\": \"cro456\"\n" +
                        "    },\n" +
                        "    \"matchType\": \"NAME_DOB\",\n" +
                        "    \"confirmed\": \"true\"\n" +
                        "}")
                .when()
                .post("/court/SHF/case/1234567891/offender-matches")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern("/court/SHF/case/1234567891/offender-matches/[0-9]*"))
                .extract()
                .header("Location");

        given()
                .accept(APPLICATION_JSON_VALUE)
                .when()
                .get(location)
                .then()
                .statusCode(200)
                .body("matchIdentifiers.crn", equalTo("X346204"))
                .body("matchIdentifiers.pnc", equalTo("pnc123"))
                .body("matchIdentifiers.cro", equalTo("cro456"))
                .body("matchType",  equalTo("NAME_DOB"))
                .body("confirmed", equalTo("true"));
    }

    @Test
    public void givenCourtDoesNotExist_whenPostMadeToOffenderMatches_thenReturnNotFound() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/court/FOO/case/1234567890/offender-matches")
                .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Court with courtCode 'FOO' not found"))
                    .body("developerMessage" , equalTo("Court with courtCode 'FOO' not found"));
    }

    @Test
    public void givenCaseDoesNotExist_whenPostMadeToOffenderMatches_thenReturnNotFound() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/court/SHF/case/1234567890/offender-matches")
                .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Case with caseNo '1234567890', courtCode 'SHF' not found"))
                    .body("developerMessage" , equalTo("Case with caseNo '1234567890', courtCode 'SHF' not found"));
    }

}
