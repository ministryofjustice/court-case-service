package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.ACCEPT_RANGES;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.justice.probation.courtcaseservice.testUtil.DateHelper.standardDateOf;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderControllerIntTest extends BaseIntTest {

    private static final String GET_DOCUMENT_PATH = "/offender/%s/documents/%s";

    private static final String KNOWN_CRN = "X320741";

    @Test
    public void givenOffenderDoesNotExist_whenCallMadeToGetProbationRecord_thenReturnNotFound() {
        given()
            .auth()
            .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/offender/CRNXXX/probation-record")
                .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Offender with CRN 'CRNXXX' not found"))
                    .body("developerMessage" , equalTo("Offender with CRN 'CRNXXX' not found"));
    }

    @Test
    public void whenCallMadeToGetProbationRecord_thenReturnCorrectData() {
        given()
            .auth()
            .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/offender/X320741/probation-record")
        .then()
                .statusCode(200)
                .body("crn",  equalTo("X320741"))
                .body("offenderManagers[0].staff.forenames", equalTo("Temperance"))
                .body("offenderManagers[0].staff.surname", equalTo("Brennan"))
                .body("offenderManagers[0].team.description", equalTo("OMIC OMU A"))
                .body("offenderManagers[0].team.telephone", equalTo("0151 222 3333"))
                .body("offenderManagers[0].team.localDeliveryUnit", equalTo("LDU Description"))
                .body("offenderManagers[0].team.district", equalTo("OMiC POM Responsibility"))
                .body("offenderManagers[0].provider", equalTo("NPS North East"))
                .body("offenderManagers[0].allocatedDate", equalTo(standardDateOf(2019, 9, 30)))

                .body("convictions[0].convictionId", equalTo("2500295345"))
                .body("convictions[0].active", equalTo(true))
                .body("convictions[0].inBreach", equalTo(true))
                .body("convictions[0].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[0].offences[1].description", equalTo("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801"))
                .body("convictions[0].sentence.sentenceId", equalTo("123457"))
                .body("convictions[0].sentence.description", equalTo("CJA - Indeterminate Public Prot."))
                .body("convictions[0].sentence.length", equalTo(5))
                .body("convictions[0].sentence.lengthUnits", equalTo("Years"))
                .body("convictions[0].sentence.lengthInDays", equalTo(1826))
                .body("convictions[0].sentence.terminationDate", equalTo(standardDateOf(2019, 1, 1)))
                .body("convictions[0].sentence.terminationReason", equalTo("ICMS Miscellaneous Event"))
                .body("convictions[0].convictionDate", equalTo(standardDateOf(2019, 9,3)))
                .body("convictions[0].documents", hasSize(1))
                .body("convictions[0].documents[0].documentId", equalTo("1d842fce-ec2d-45dc-ac9a-748d3076ca6b"))
                .body("convictions[0].breaches", hasSize(0))
                .body("convictions[0].endDate", equalTo(standardDateOf(2019, 1,1)))
                .body("convictions[0].sentence.endDate", equalTo(standardDateOf(2019, 1,1)))

                .body("convictions[1].convictionId", equalTo("2500297061"))
                .body("convictions[1].active", equalTo(false))
                .body("convictions[1].inBreach", equalTo(true))
                .body("convictions[1].offences[0].description", equalTo("Assault on Police Officer - 10400"))
                .body("convictions[1].sentence.sentenceId", equalTo("123456"))
                .body("convictions[1].sentence.description", equalTo("Absolute/Conditional Discharge"))
                .body("convictions[1].sentence.length", equalTo(0))
                .body("convictions[1].sentence.lengthUnits", equalTo("Months"))
                .body("convictions[1].sentence.lengthInDays", equalTo(0))
                .body("convictions[1].sentence.terminationDate", equalTo(standardDateOf(2020, 1, 1)))
                .body("convictions[1].sentence.terminationReason", equalTo("Auto Terminated"))
                .body("convictions[1].sentence", not(hasKey("unpaidWork")))
                .body("convictions[1].convictionDate", equalTo(standardDateOf(2019, 9,16)))
                .body("convictions[1].documents", hasSize(0))
                .body("convictions[1].breaches", hasSize(2))
                .body("convictions[1].breaches[0].breachId", equalTo(11131322))

                .body("convictions[2].convictionId", equalTo("2500295343"))
                .body("convictions[2].active", equalTo(false))
                .body("convictions[2].inBreach", equalTo(true))
                .body("convictions[2].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[2].convictionDate", equalTo(null))
                .body("convictions[2].documents", hasSize(0))
                .body("convictions[2].breaches", hasSize(2))
                .body("convictions[2].breaches[0].breachId", equalTo(11131322))
                .body("convictions[2].breaches[0].description", equalTo("Community Order"))
                .body("convictions[2].breaches[0].started", equalTo(standardDateOf(2020, 10, 20)))
                .body("convictions[2].breaches[0].status", equalTo("Breach Initiated"))
                .body("convictions[2].breaches[0].statusDate", equalTo(standardDateOf(2020, 12, 18)))
                .body("convictions[2].breaches[1].statusDate", equalTo(standardDateOf(2019, 12, 18)))
        ;
    }

    @Test
    public void whenCallMadeToGetProbationRecordNotFiltered_thenReturnExtraDocuments() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/offender/X320741/probation-record?applyDocTypeFilter=false")
            .then()
                .statusCode(200)
                .body("crn",  equalTo("X320741"))
                .body("convictions.size()", is(3))

                .body("convictions[0].convictionId", equalTo("2500295345"))
                .body("convictions[0].documents", hasSize(8))

                .body("convictions[1].convictionId", equalTo("2500297061"))
                .body("convictions[1].documents", hasSize(0))

                .body("convictions[2].convictionId", equalTo("2500295343"))
                .body("convictions[2].documents", hasSize(7))
        ;
    }

    @Test
    public void whenCallMadeToGetProbationStatusDetail_thenReturn() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/offender/X320741/probation-status-detail")
            .then()
            .statusCode(200)
            .body("probationStatus",  equalTo("Current"))
            .body("inBreach",  equalTo(true))
        ;
    }

    @Test
    public void givenOffenderDoesNotExist_whenCallMadeToGetProbationStatusDetail_thenReturnNotFound() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/offender/CRNXXX/probation-status-detail")
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Offender with CRN 'CRNXXX' not found"))
            .body("developerMessage" , equalTo("Offender with CRN 'CRNXXX' not found"));
    }

    @Test
    public void singleDocument_givenExistingDocumentIdThenReturn200AndHeaders() {
        final byte[] bytes =
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(String.format(GET_DOCUMENT_PATH, KNOWN_CRN, "abc-def"))
        .then()
            .statusCode(HttpStatus.OK.value())
            .contentType("application/msword;charset=UTF-8")
            .header(CONTENT_DISPOSITION, "attachment; filename=\"sample_word_doc.doc\"")
            .header(ACCEPT_RANGES, "bytes")
            .header(LAST_MODIFIED, "Wed, 03 Jan 2018 13:20:35 GMT")
            .header("Server", "Apache-Coyote/1.1")
            .extract()
            .asByteArray();

        assertThat(bytes.length).isEqualTo(20992);
    }

    @Test
    public void singleDocument_givenUnknownDocumentIdThenReturn404() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(String.format(GET_DOCUMENT_PATH, KNOWN_CRN, "xxx"))
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void singleDocument_givenServerError() {
        given()
            .auth()
            .oauth2(getToken())
            .contentType(APPLICATION_JSON_VALUE)
        .when()
            .get(String.format(GET_DOCUMENT_PATH, "X320500", "abc-def"))
        .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void whenCallMadeToGetOffenderDetail_thenReturnCorrectData() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/offender/X320741/detail")
            .then()
            .statusCode(200)
            .body("title",  equalTo("Mr."))
            .body("probationStatus", equalTo("Current"))
            .body("dateOfBirth", equalTo("2000-07-19"))
            .body("forename", equalTo("Aadland"))
            .body("surname", equalTo("Bertrand"))
            .body("otherIds.crn", equalTo("X320741"))
            .body("otherIds.offenderId", equalTo(2500343964L))
            .body("otherIds.pncNumber", equalTo("2004/0712343H"))
            .body("otherIds.croNumber", equalTo("123456/04A"))
        ;
    }

    @Test
    public void givenOffenderDoesNotExist_whenCallMadeToGetOffenderDetail_thenReturnNotFound() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/offender/CRNXXX/detail")
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Offender with CRN 'CRNXXX' not found"))
            .body("developerMessage" , equalTo("Offender with CRN 'CRNXXX' not found"));
    }

}
