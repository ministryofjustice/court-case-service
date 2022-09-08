package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = {"classpath:sql/before-common.sql", "classpath:case-progress.sql"}, config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseProgressIntTest extends BaseIntTest {

    @Test
    void givenExistingCaseId_whenGetHearingByDefendantId_thenReturnCaseSummaryAlongWithAllHearings() {

        String testCaseId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        String defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";
        String testHearingId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        var response = given()
            .given()
            .auth()
            .oauth2(getToken())
            .when()
            .header("Accept", "application/json")
            .get("/hearing/{hearingId}/defendant/{defendantId}", testHearingId, defendantId)
            .then()
            .statusCode(200);

        response
            .body("caseId", equalTo(testCaseId))
            .body("hearingId", equalTo(testCaseId))
            .body("hearingType", equalTo("Sentence"))
            .body("urn", equalTo("URN008"))
            .body("offences", hasSize(2))
            .body("probationStatus", equalTo("Current"))
            .body("probationStatusActual", equalTo("CURRENT"))
            .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, Month.JANUARY, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("preSentenceActivity", equalTo(true))
            .body("suspendedSentenceOrder", equalTo(true))
            .body("breach", equalTo(true))
            .body("source", equalTo("COMMON_PLATFORM"))
            .body("crn", equalTo("X320741"))
            .body("pnc", equalTo("A/1234560BA"))
            .body("cro", equalTo("311462/13E"))
            .body("defendantId", equalTo(defendantId))
            .body("phoneNumber.mobile", equalTo("07000000007"))
            .body("phoneNumber.home", equalTo("07000000013"))
            .body("phoneNumber.work", equalTo("07000000015"))
            .body("name.title", equalTo("Mr"))
            .body("name.forename1", equalTo("Johnny"))
            .body("name.forename2", equalTo("John"))
            .body("name.forename3", equalTo("Jon"))
            .body("name.surname", equalTo("BALL"))

            .body("hearings", hasSize(2))
            .body("hearings[0].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
            .body("hearings[0].court", equalTo("North Shields"))
            .body("hearings[0].courtRoom", equalTo("2"))
            .body("hearings[0].session", equalTo("MORNING"))
            .body("hearings[0].hearingTypeLabel", equalTo("Sentence"))
            .body("hearings[0].hearingDateTime", equalTo("2019-11-14T09:00:00"))

            .body("hearings[1].hearingId", equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].court", equalTo("Leicester"))
            .body("hearings[1].courtRoom", equalTo("2"))
            .body("hearings[1].session", equalTo("MORNING"))
            .body("hearings[1].hearingTypeLabel", equalTo("Hearing"))
            .body("hearings[1].hearingDateTime", equalTo("2019-10-14T09:00:00"))
        ;
    }
}
