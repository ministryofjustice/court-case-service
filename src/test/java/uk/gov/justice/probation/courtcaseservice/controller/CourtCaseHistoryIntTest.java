package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseHistoryIntTest extends BaseIntTest {

    private static final LocalDate DECEMBER_14 = LocalDate.of(2019, Month.DECEMBER, 14);
    private static final String CASE_NO = "1600028913";

    @Test
    void givenExistingCaseId_whenGetCaseHistory_thenReturnCaseHistory() {

        String testCaseId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        String defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";
        given()
            .auth()
            .oauth2(getToken())
        .when()
            .header("Accept", "application/json")
            .get("/cases/{caseId}", testCaseId)
        .then()
            .statusCode(200)
            .body("caseId", equalTo(testCaseId))
            .body("urn", equalTo("URN008"))
            .body("caseNo", equalTo(CASE_NO))
            .body("source", equalTo("LIBRA"))
            .body("defendants", hasSize(1))
            .body("defendants[0].type", equalTo("PERSON"))
            .body("defendants[0].defendantId", equalTo(defendantId))
            .body("defendants[0].phoneNumber.mobile", equalTo("07000000007"))
            .body("defendants[0].phoneNumber.home", equalTo("07000000013"))
            .body("defendants[0].phoneNumber.work", equalTo("07000000015"))
            .body("defendants[0].name.title", equalTo("Mr"))
            .body("defendants[0].name.forename1", equalTo("Johnny"))
            .body("defendants[0].name.surname", equalTo("BALL"))
            .body("defendants[0].offender.crn", equalTo("X320741"))
            .body("defendants[0].offender.pnc", equalTo("PNCINT007"))
            .body("defendants[0].offender.probationStatus", equalTo("CURRENT"))
            .body("defendants[0].offender.crn", equalTo("X320741"))

            .body("hearings", hasSize(2))
            .body("hearings[0].hearingId", equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[0].hearingUpdates", hasSize(1 ))
            .body("hearings[0].hearingUpdates[0].defendantIds", equalTo(List.of(defendantId)))
            .body("hearings[0].hearingUpdates[0].offences", hasSize(2))
            .body("hearings[0].hearingUpdates[0].offences[0].title", equalTo("Theft from a shop"))
            .body("hearings[0].hearingUpdates[0].offences[1].title", equalTo("Theft from a different shop"))

            .body("hearings[1].hearingId", equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
            .body("hearings[1].hearingUpdates", hasSize(2 ))
            .body("hearings[1].hearingUpdates[0].defendantIds", equalTo(List.of(defendantId)))
            .body("hearings[1].hearingUpdates[0].defendantIds", equalTo(List.of(defendantId)))
            .body("hearings[1].hearingUpdates[0].offences", hasSize(2))
            .body("hearings[1].hearingUpdates[0].offences[0].title", equalTo("Theft from a shop"))
            .body("hearings[1].hearingUpdates[0].offences[1].title", equalTo("Theft from a different shop"))
            .body("hearings[1].hearingUpdates[0].hearingDays", hasSize(1))
            .body("hearings[1].hearingUpdates[0].hearingDays[0].courtCode", equalTo(EntityHelper.COURT_CODE))
            .body("hearings[1].hearingUpdates[0].hearingDays[0].courtRoom", equalTo(EntityHelper.COURT_ROOM))
            .body("hearings[1].hearingUpdates[0].hearingDays[0].listNo", equalTo("3rd"))
            .body("hearings[1].hearingUpdates[0].hearingDays[0].sessionStartTime", equalTo(LocalDateTime.of(DECEMBER_14, LocalTime.of(9, 0)).format(DateTimeFormatter.ISO_DATE_TIME)))

            .body("hearings[1].hearingUpdates[1].defendantIds", equalTo(List.of(defendantId)))
            .body("hearings[1].hearingUpdates[1].offences", hasSize(1))
            .body("hearings[1].hearingUpdates[1].offences[0].title", equalTo("Theft from a different shop"))
        ;
    }
}
