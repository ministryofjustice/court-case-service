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
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.CRN;
import static uk.gov.justice.probation.courtcaseservice.testUtil.DateHelper.standardDateOf;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderController_RegistrationIntTest extends BaseIntTest {

    private static final String PATH = "/offender/%s/registrations";

    @Test
    public void whenGetRegistrations_thenReturn() {

        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(PATH, CRN))
            .then()
            .statusCode(200)
            .body("$.size()", is(4))
            .body("[0].startDate", equalTo(standardDateOf(2020, 9, 30)))
            .body("[0].type", equalTo("Suicide/Self Harm"))
            .body("[0].nextReviewDate", equalTo(standardDateOf(2021, 3, 30)))
            .body("[0].active", is(true))
            .body("[0].notes[0]", equalTo("Note 1"))
            .body("[0].notes[1]", equalTo("Notes added second"))
            .body("[3].active", is(false))
            .body("[3].endDate", equalTo(standardDateOf(2019, 10, 14)))
        ;

    }

    @Test
    public void givenUnknownCrn_whenGetRegistrations_thenReturn404() {

        final String getPath = String.format(PATH, "CRNXXX");
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(getPath)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
        ;
    }
}
