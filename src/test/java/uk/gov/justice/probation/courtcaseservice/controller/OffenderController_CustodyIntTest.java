package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

class OffenderController_CustodyIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String CRN_NO_NOMS = "X980123";
    private static final String CRN_SERVER_ERROR = "X980456";
    private static final String CONVICTION_ID = "2500295343";
    private static final String CONVICTION_ID_NO_CUSTODY = "2500295399";
    private static final String GET_CUSTODY_PATH = "/offender/%s/convictions/%s/sentence/custody";

    @Test
    void whenCallMadeToGetCustody_thenReturnCustodyData() {

        String path = String.format(GET_CUSTODY_PATH, CRN, CONVICTION_ID);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get(path)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("homeDetentionCurfewActualDate", equalTo("2021-07-16"))
                .body("homeDetentionCurfewEndDate", equalTo("2021-07-17"))
                .body("licenceExpiryDate", equalTo("2021-07-18"))
                .body("releaseDate", equalTo("2021-07-19"))
                .body("topupSupervisionStartDate", equalTo("2021-07-20"))
                .body("topupSupervisionExpiryDate", equalTo("2021-07-21"))
        ;
    }

    @Test
    void whenCallMadeToGetCustody_andNoCustodyElementInCommunityApi_thenReturnNotFound() {

        String path = String.format(GET_CUSTODY_PATH, CRN, CONVICTION_ID_NO_CUSTODY);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get(path)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
        ;
    }

    @Test
    void whenCallMadeToGetCustody_andNoNomsNumber_thenReturnServerError() {
        String path = String.format(GET_CUSTODY_PATH, CRN_NO_NOMS, CONVICTION_ID);
        given()
                .auth()
                .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void whenCallMadeToGetCustody_andServerErrorFromPrisonApi_thenReturnServerError() {
        String path = String.format(GET_CUSTODY_PATH, CRN_SERVER_ERROR, CONVICTION_ID);
        given()
                .auth()
                .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
