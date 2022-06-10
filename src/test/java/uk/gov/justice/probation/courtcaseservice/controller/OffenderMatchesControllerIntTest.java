package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.controller.OffenderMatchesControllerTest.OFFENDER_MATCHES_BY_DEFENDANT_ID_DETAIL_PATH;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class OffenderMatchesControllerIntTest extends BaseIntTest {

    private static final String GET_GROUPED_OFFENDER_MATCHES_BY_DEFENDANT_ID_AND_GROUP_ID_PATH = "/defendant/%s/grouped-offender-matches/%s";

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

    public static final String MULTIPLE_NON_EXACT_MATCH_BODY = "{\n" +
            "  \"matches\": [\n" +
            "    {\n" +
            "      \"matchIdentifiers\": {\n" +
            "        \"crn\": \"X12345\"\n" +
            "      },\n" +
            "      \"matchType\": \"PARTIAL_NAME\",\n" +
            "      \"confirmed\": \"false\",\n" +
            "      \"rejected\": \"false\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"matchIdentifiers\": {\n" +
            "        \"crn\": \"X12346\"\n" +
            "      },\n" +
            "      \"matchType\": \"NAME_DOB_ALIAS\",\n" +
            "      \"confirmed\": \"false\",\n" +
            "      \"rejected\": \"false\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"matchIdentifiers\": {\n" +
            "        \"crn\": \"X346204\",\n" +
            "        \"pnc\": \"pnc123\",\n" +
            "        \"cro\": \"cro456\",\n" +
            "        \"aliases\": [\n" +
            "          {\n" +
            "            \"dateOfBirth\": \"1969-08-26\",\n" +
            "            \"firstName\": \"Adi\",\n" +
            "            \"surname\": \"Akinbye\",\n" +
            "            \"gender\": \"Male\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"dateOfBirth\": \"1968-08-06\",\n" +
            "            \"firstName\": \"Chris\",\n" +
            "            \"surname\": \"FAULKNER\",\n" +
            "            \"gender\": \"Male\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matchType\": \"NAME_DOB_ALIAS\",\n" +
            "      \"confirmed\": true,\n" +
            "      \"rejected\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Nested
    class MatchDetailByDefendantId {

        private static final String DEFENDANT_ID = "b6e663c5-d2be-434b-b597-3e98a112af9f";

        @Test
        void givenMultipleMatchesFoundForDefendantId_whenGetOffenderDetailMatch_fetchLatestByGroupId_thenReturn200() {

            String path = String.format(OFFENDER_MATCHES_BY_DEFENDANT_ID_DETAIL_PATH, DEFENDANT_ID);
            final var validatableResponse = given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get(path)
                    .then()
                    .statusCode(200);

            validatableResponse.body("offenderMatchDetails", hasSize(1))
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
                .body("offenderMatchDetails[0].probationStatus", equalTo("Previously known"))
                .body("offenderMatchDetails[0].probationStatusActual", equalTo("PREVIOUSLY_KNOWN"))
                .body("offenderMatchDetails[0].mostRecentEvent.text", equalTo("CJA - Indeterminate Public Prot."))
                .body("offenderMatchDetails[0].mostRecentEvent.length", equalTo(5))
                .body("offenderMatchDetails[0].mostRecentEvent.lengthUnits", equalTo("Years"))
                .body("offenderMatchDetails[0].mostRecentEvent.startDate", equalTo("2014-01-01"))
                .body("offenderMatchDetails[0].matchIdentifiers.aliases", hasSize(2))
                .body("offenderMatchDetails[0].matchIdentifiers.aliases[0].gender", equalTo("Male"))
                .body("offenderMatchDetails[0].matchIdentifiers.aliases[0].firstName", equalTo("Aliasone"))
                .body("offenderMatchDetails[0].matchIdentifiers.aliases[1].firstName", equalTo("Aliastwo"))
                .body("offenderMatchDetails[0].matchIdentifiers.aliases[1].dateOfBirth", equalTo("2022-05-17"))
            ;
        }

        @Test
        void givenMatchWithNoConvictions_whenGetOffenderDetailMatch_thenReturn200WithNoMostRecentEvent() {

            String path = String.format(OFFENDER_MATCHES_BY_DEFENDANT_ID_DETAIL_PATH, "b3f7e2bc-c8f9-4031-9c0c-928c41193acd");
            final var validatableResponse = given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get(path)
                    .then()
                    .statusCode(200);

            validatableResponse.body("offenderMatchDetails", hasSize(1))
                .body("offenderMatchDetails[0].title", equalTo(null))
                .body("offenderMatchDetails[0].forename", equalTo("Nic"))
                .body("offenderMatchDetails[0].middleNames", hasSize(0))
                .body("offenderMatchDetails[0].surname", equalTo("Cage"))
                .body("offenderMatchDetails[0].dateOfBirth", equalTo("1965-07-19"))
                .body("offenderMatchDetails[0].address", equalTo(null))
                .body("offenderMatchDetails[0].matchIdentifiers.crn", equalTo("X980123"))
                .body("offenderMatchDetails[0].probationStatus", equalTo("Previously known"))
                .body("offenderMatchDetails[0].probationStatusActual", equalTo("PREVIOUSLY_KNOWN"))
                .body("offenderMatchDetails[0].mostRecentEvent", equalTo(null));
        }

        @Test
        void givenDefendantIdDoesNotExist_whenGetOffenderDetailMatch_thenReturnNotFound() {
            String path = String.format(OFFENDER_MATCHES_BY_DEFENDANT_ID_DETAIL_PATH, "90db99d6-04db-11ec-b2d8-0242ac130002");
            given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get(path)
                    .then()
                    .statusCode(404)
                    .body("userMessage", equalTo("Defendant 90db99d6-04db-11ec-b2d8-0242ac130002 not found"))
                    .body("developerMessage", equalTo("Defendant 90db99d6-04db-11ec-b2d8-0242ac130002 not found"));
        }
    }

    @Nested
    class GetGroupedOffenderMatchesEntity {

        private static final String GROUP_ID = "9999991";
        private static final String DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002";

        private static final String DEFENDANT_ID_NOT_EXIST = "40db17d6-04db";


        @Test
        void givenDefendantIdMatch_whenGetGroupedMatchesByDefendantId_thenReturn200() {

            String path = String.format(GET_GROUPED_OFFENDER_MATCHES_BY_DEFENDANT_ID_AND_GROUP_ID_PATH, DEFENDANT_ID, GROUP_ID);
            final var validatableResponse = given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get(path)
                    .then()
                    .statusCode(200);

            validatableResponse.body("defendantId", equalTo(DEFENDANT_ID));
        }

        @Test
        void givenDefendantIdDoesNotExist_whenGetOffenderDetailMatch_thenReturnNotFound() {
            String path = String.format(GET_GROUPED_OFFENDER_MATCHES_BY_DEFENDANT_ID_AND_GROUP_ID_PATH, DEFENDANT_ID_NOT_EXIST, GROUP_ID);
            given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get(path)
                    .then()
                    .statusCode(404)
                    .body("developerMessage", equalTo(String.format("Grouped Matches %s not found for defendant %s", GROUP_ID, DEFENDANT_ID_NOT_EXIST)));
        }
    }

    @Nested
    class CreateOrUpdateGroupedMatchesByDefendant {

        private static final String DEFENDANT_ID = "3e94df33-8165-448b-ade9-14a28408e377";

        private static final String DEFENDANT_ID_NOT_EXIST = "40db17d6-04db-11ec-b2d8-0242ac130003";

        @Test
        void givenCourtCaseExistsWithNoPriorMatches_whenPostMadeToOffenderMatches_thenReturn201CreatedWithValidLocation() {
            String location = given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(SINGLE_EXACT_MATCH_BODY)
                    .when()
                    .post("/defendant/" + DEFENDANT_ID + "/grouped-offender-matches")
                    .then()
                    .statusCode(201)
                    .header("Location", matchesPattern("/defendant/" + DEFENDANT_ID + "/grouped-offender-matches/[0-9]+"))
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
                    .body("offenderMatches", hasSize(1))
                    .body("offenderMatches[0].crn", equalTo("X346204"))
                    .body("offenderMatches[0].pnc", equalTo("pnc123"))
                    .body("offenderMatches[0].cro", equalTo("cro456"))
                    .body("offenderMatches[0].matchType", equalTo("NAME_DOB"))
                    .body("offenderMatches[0].confirmed", equalTo(true));
        }

        @Test
        void givenCourtCaseExistsWithPriorMatches_whenPostMadeToOffenderMatches_thenReturn201CreatedWithValidLocation() {


            String defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";

            String location = given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(MULTIPLE_NON_EXACT_MATCH_BODY)
                    .when()
                    .post("/defendant/" + defendantId + "/grouped-offender-matches")
                    .then()
                    .statusCode(201)
                    .header("Location", matchesPattern("/defendant/" + defendantId + "/grouped-offender-matches/[0-9]+"))
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
                    .body("offenderMatches", hasSize(3))
                    .body("offenderMatches[0].crn", equalTo("X12345"))
                    .body("offenderMatches[0].pnc", equalTo(null))
                    .body("offenderMatches[0].cro", equalTo(null))
                    .body("offenderMatches[0].matchType", equalTo("PARTIAL_NAME"))
                    .body("offenderMatches[0].confirmed", equalTo(false))
                    .body("offenderMatches[0].rejected", equalTo(false))
                    .body("offenderMatches[1].matchType", equalTo("NAME_DOB_ALIAS"))
                    .body("offenderMatches[2].matchType", equalTo("NAME_DOB_ALIAS"))
                    .body("offenderMatches[2].aliases", hasSize(2))
                    .body("offenderMatches[2].aliases[0].gender", equalTo("Male"))
                    .body("offenderMatches[2].aliases[1].dateOfBirth", equalTo("1968-08-06"))
            ;
        }

        @Test
        void givenNoHearingEntityExistForDefendant_whenPostMadeToOffenderMatches_thenReturn201CreatedWithValidLocation() {

            given()
                    .auth()
                    .oauth2(getToken())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(SINGLE_EXACT_MATCH_BODY)
                    .when()
                    .post("/defendant/" + DEFENDANT_ID_NOT_EXIST + "/grouped-offender-matches")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", equalTo(String.format("Hearing  entity not found for defendant %s", DEFENDANT_ID_NOT_EXIST)));
        }

    }

}
