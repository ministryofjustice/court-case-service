package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.controller.OffenderMatchesControllerTest.OFFENDER_MATCHES_DETAIL_PATH;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class OffenderMatchesControllerIntTest extends BaseIntTest {

    private static final String COURT_CODE = "SHF";
    private static final String CASE_NO = "1600028913";

    public static final String SINGLE_EXACT_MATCH_BODY = "{\n" +
            "    \"matches\": [\n" +
            "        {\n" +
            "                \"matchIdentifiers\": {\n" +
            "                \"crn\": \"X346204\",\n" +
            "                \"pnc\": \"pnc123\",\n" +
            "                \"cro\": \"cro456\"\n" +
            "            },\n" +
            "            \"matchType\": \"NAME_DOB\",\n" +
            "            \"confirmed\": \"true\",\n" +
            "            \"rejected\": \"false\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    @Test
    public void givenCaseExists_whenPostMadeToOffenderMatches_thenReturn201CreatedWithValidLocation() {
        String location = given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .body(SINGLE_EXACT_MATCH_BODY)
            .when()
                .post("/court/SHF/case/1600028913/grouped-offender-matches")
            .then()
                .statusCode(201)
                .header("Location", matchesPattern("/court/SHF/case/1600028913/grouped-offender-matches/[0-9]+"))
                .extract()
                .header("Location");

        given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
            .when()
                .get(location)
            .then()
                .statusCode(200)
                .body("courtCase.courtCode", equalTo("SHF"))
                .body("courtCase.caseNo", equalTo("1600028913"))
                .body("offenderMatches[0].crn", equalTo("X346204"))
                .body("offenderMatches[0].pnc", equalTo("pnc123"))
                .body("offenderMatches[0].cro", equalTo("cro456"))
                .body("offenderMatches[0].matchType",  equalTo("NAME_DOB"))
                .body("offenderMatches[0].confirmed", equalTo(true));
    }

    @Test
    public void givenCourtDoesNotExist_whenPostMadeToOffenderMatches_thenReturnNotFound() {
        given()
                .auth()
                .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .body(SINGLE_EXACT_MATCH_BODY)
            .when()
                .post("/court/FOO/case/1234567890/grouped-offender-matches")
            .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Court FOO not found"))
                    .body("developerMessage" , equalTo("Court FOO not found"));
    }

    @Test
    public void givenCaseDoesNotExist_whenPostMadeToOffenderMatches_thenReturnNotFound() {
        given()
                .auth()
                .oauth2(getToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .body(SINGLE_EXACT_MATCH_BODY)
            .when()
                .post("/court/SHF/case/1234567890/grouped-offender-matches")
            .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Case 1234567890 not found for court SHF"))
                    .body("developerMessage" , equalTo("Case 1234567890 not found for court SHF"));
    }

    @Test
    public void givenMultipleMatchesOneNotFound_whenGetOffenderDetailMatch_thenReturn200() {

        String path = String.format(OFFENDER_MATCHES_DETAIL_PATH, COURT_CODE, CASE_NO);
        given()
            .auth()
            .oauth2(getToken())
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body("offenderMatchDetails", hasSize(1))
            .body("offenderMatchDetails[0].title", equalTo("Mr."))
            .body("offenderMatchDetails[0].forename", equalTo("Aadland"))
            .body("offenderMatchDetails[0].middleNames", hasSize(2))
            .body("offenderMatchDetails[0].middleNames[0]", equalTo("Felix"))
            .body("offenderMatchDetails[0].middleNames[1]", equalTo("Hope"))
            .body("offenderMatchDetails[0].surname", equalTo("Bertrand"))
            .body("offenderMatchDetails[0].dateOfBirth", equalTo("2000-07-19"))
            .body("offenderMatchDetails[0].address.addressNumber", equalTo("19"))
            .body("offenderMatchDetails[0].address.streetName", equalTo("Junction Road"))
            .body("offenderMatchDetails[0].address.district", equalTo("Blackheath"))
            .body("offenderMatchDetails[0].address.town", equalTo("Sheffield"))
            .body("offenderMatchDetails[0].address.county", equalTo("South Yorkshire"))
            .body("offenderMatchDetails[0].address.postcode", equalTo("S10 2NA"))
            .body("offenderMatchDetails[0].matchIdentifiers.crn", equalTo("X320741"))
            .body("offenderMatchDetails[0].matchIdentifiers.pnc", equalTo("2004/0712343H"))
            .body("offenderMatchDetails[0].matchIdentifiers.cro", equalTo("123456/04A"))
            .body("offenderMatchDetails[0].probationStatus", equalTo("Current"))
            .body("offenderMatchDetails[0].mostRecentEvent.text", equalTo("CJA - Indeterminate Public Prot."))
            .body("offenderMatchDetails[0].mostRecentEvent.length", equalTo(5))
            .body("offenderMatchDetails[0].mostRecentEvent.lengthUnits", equalTo("Years"))
            .body("offenderMatchDetails[0].mostRecentEvent.startDate", equalTo("2014-01-01"))
            ;
    }

    @Test
    public void givenCaseDoesNotExist_whenGetOffenderDetailMatch_thenReturnNotFound() {
        String path = String.format(OFFENDER_MATCHES_DETAIL_PATH, COURT_CODE, "23456541141414");
        given()
            .auth()
            .oauth2(getToken())
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Case 23456541141414 not found for court SHF"))
            .body("developerMessage" , equalTo("Case 23456541141414 not found for court SHF"));
    }

}
