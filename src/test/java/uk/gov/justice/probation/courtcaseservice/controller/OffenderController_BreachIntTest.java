package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderController_BreachIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String CONVICTION_ID = "2500295343";
    private static final String BREACH_ID = "2500003903";
    private static final String UNKNOWN_CRN = "CRNXXX";

    private static final String GET_BREACH_PATH = "/offender/%s/convictions/%s/breaches/%s";

    @Test
    public void whenCallMadeToGetBreach_thenReturnCorrectData() {

        String path = String.format(GET_BREACH_PATH, CRN, CONVICTION_ID, BREACH_ID);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get(path)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("breachId", equalTo(2500003903L))
                .body("incidentDate", equalTo("2017-03-21"))
                .body("statusDate", equalTo("2017-05-22"))
                .body("started", equalTo("2017-03-22"))
                .body("notes", equalTo("Paragraph 1: Some information.\nParagraph 2: And some more."))
                .body("provider", equalTo("CPA West Yorkshire"))
                .body("team", equalTo("Unallocated"))
                .body("officer", equalTo("Unallocated Staff"))
                .body("status", equalTo("Induction Completed - Opted Out"))
                .body("order", equalTo("CJA - Community Order (12 Months)"))
                .body("sentencingCourtName", equalTo("Bicester Magistrates Court"))
                .body("documents", hasSize(1))
        ;
    }

    @Test
    public void givenUnknownBreachId_whenGetBreach_thenReturn404() {
        // Three of the four API calls return 200 status code but the breach ID is unknown
        String path = String.format(GET_BREACH_PATH, CRN, CONVICTION_ID, "0");

        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(404)
            .body("developerMessage", equalTo("Nsi with id '0' not found for convictionId '2500295343' and crn 'X320741'"))
        ;
    }

    @Test
    public void givenUnknownCrn_whenGetBreach_thenReturn404() {

        String path = String.format(GET_BREACH_PATH, UNKNOWN_CRN, CONVICTION_ID, BREACH_ID);

        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(path)
        .then()
            .statusCode(404)
        ;
    }

    @Test
    public void whenBreachThrowsServerError_thenReturn500() {
        // Three of the four calls here will return data with 200 status code
        // but the NSI one gives a 500 status code
        String path = String.format(GET_BREACH_PATH, CRN, CONVICTION_ID, "500");

        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(path)
        .then()
            .statusCode(500)
        ;
    }

}
