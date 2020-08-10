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
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class OffenderMatchesControllerIntTest extends BaseIntTest {

    public static final String SINGLE_EXACT_MATCH_BODY = "{\n" +
            "    \"matches\": [\n" +
            "        {\n" +
            "                \"matchIdentifiers\": {\n" +
            "                \"crn\": \"X346204\",\n" +
            "                \"pnc\": \"pnc123\",\n" +
            "                \"cro\": \"cro456\"\n" +
            "            },\n" +
            "            \"matchType\": \"NAME_DOB\",\n" +
            "            \"confirmed\": \"true\"\n" +
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
}
