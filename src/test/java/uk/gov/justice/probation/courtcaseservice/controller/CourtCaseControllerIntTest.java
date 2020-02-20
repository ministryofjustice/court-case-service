package uk.gov.justice.probation.courtcaseservice.controller;

        import com.fasterxml.jackson.databind.ObjectMapper;
        import io.restassured.http.ContentType;
        import org.junit.Before;
        import org.junit.Test;
        import org.junit.runner.RunWith;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.test.context.SpringBootTest;
        import org.springframework.boot.web.server.LocalServerPort;
        import org.springframework.test.context.ActiveProfiles;
        import org.springframework.test.context.jdbc.Sql;
        import org.springframework.test.context.jdbc.SqlConfig;
        import org.springframework.test.context.junit4.SpringRunner;
        import uk.gov.justice.probation.courtcaseservice.TestConfig;
        import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
        import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.time.format.DateTimeFormatter;

        import static io.restassured.RestAssured.given;
        import static io.restassured.RestAssured.when;
        import static org.assertj.core.api.Assertions.assertThat;
        import static org.hamcrest.Matchers.*;
        import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
        import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest {

    /* before-test.sql sets up a court case in the database */

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private final String COURT_CODE = "SHF";
    private final String CASE_ID = "123456";
    private final String NEW_CASE_ID = "654321";
    private final String CASE_NO = "1600028913";
    private final String NEW_CASE_NO = "1700028914";
    private final String PROBATION_STATUS = "Previously known";
    private final String NOT_FOUND_COURT_CODE = "LPL";
    private final LocalDateTime now = LocalDateTime.now();
    private final CourtCaseEntity caseDetails = new CourtCaseEntity();

    @Before
    public void setup() {
        TestConfig.configureRestAssuredForIntTest(port);

        caseDetails.setCaseId(NEW_CASE_ID);
        caseDetails.setCaseNo(NEW_CASE_NO);
        caseDetails.setCourtCode(COURT_CODE);
        caseDetails.setCourtRoom("1");
        caseDetails.setSessionStartTime(now);
        caseDetails.setProbationStatus(PROBATION_STATUS);
        caseDetails.setData("{}");
        caseDetails.setLastUpdated(now);
        caseDetails.setPreviouslyKnownTerminationDate(LocalDate.of(2010,1,1));
        caseDetails.setSuspendedSentenceOrder(true);
    }

    @Test
    public void cases_shouldGetCaseListWhenCasesExist() {

        when()
                .get("/court/{courtCode}/cases?date={date}", "SHF", LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[1].lastUpdated", containsString(now.format(DateTimeFormatter.ISO_DATE)))
                .body("cases[0].caseId", equalTo("5555555"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    @Test
    public void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
        when()
                .get("/court/{courtCode}/cases?date={date}", "SHF", "2020-02-02")
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", empty());

    }

    @Test
    public void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
        when()
                .get("/court/{courtCode}/cases", "SHF")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Required LocalDate parameter 'date' is not present"));
    }

    @Test
    public void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
        ErrorResponse result = when()
                .get("/court/{courtCode}/cases?date={date}", NOT_FOUND_COURT_CODE, "2020-02-02")
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void GET_case_shouldGetProbationStatusWhenExists() {

        when()
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat()
                .statusCode(200)
                .body("probationStatus", equalTo(PROBATION_STATUS));

    }

    @Test
    public void GET_case_shouldGetPreviouslyKnownTerminationDateWhenExists() {

        when()
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat()
                .statusCode(200)
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)));

    }

    @Test
    public void GET_case_shouldGetSuspendedSentenceOrderWhenExists() {

        when()
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat()
                .statusCode(200)
                .body("suspendedSentenceOrder", equalTo(true));

    }

    @Test
    public void shouldGetCaseWhenCourtExists() {
        given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() {
        given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .statusCode(200)
                .body("caseNo", equalTo(CASE_NO));
    }


    @Test
    public void shouldReturnNotFoundForNonexistentCase() {

        String NOT_FOUND_CASE_NO = "11111111111";

        ErrorResponse result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, NOT_FOUND_CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getUserMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentCourt() {
        ErrorResponse result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", NOT_FOUND_COURT_CODE, CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void createCaseDataWithIncorrectCourt() {

        caseDetails.setCourtCode(NOT_FOUND_COURT_CODE);

        ErrorResponse result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + CASE_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void createCaseDataWithCaseIdMismatch() {
        String mismatchCaseId = "000000";
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + mismatchCaseId)
                .then()
                .statusCode(500)
                .body("message", equalTo("Case ID " + mismatchCaseId + " does not match with " + NEW_CASE_ID));
    }

    @Test
    public void createCaseDataWithDuplicateCaseNo() {

        String newCaseId = "666666";
        caseDetails.setCaseId(newCaseId);
        caseDetails.setCaseNo(CASE_NO);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + newCaseId)
                .then()
                .statusCode(500)
                .body("message", containsString("constraint [court_case_case_no_idempotent]"));
    }

    @Test
    public void createCaseData() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(200)
                .body("caseId", equalTo(NEW_CASE_ID))
                .body("caseNo", equalTo(NEW_CASE_NO))
                .body("courtCode", equalTo(COURT_CODE))
                .body("courtRoom", equalTo("1"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(now.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true));

    }

    @Test
    public void updateCaseData() {

        createCaseData();

        caseDetails.setCourtRoom("2");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetails)
                .when()
                .put("/case/" + NEW_CASE_ID)
                .then()
                .statusCode(200)
                .body("caseId", equalTo(NEW_CASE_ID))
                .body("caseNo", equalTo(NEW_CASE_NO))
                .body("courtCode", equalTo(COURT_CODE))
                .body("courtRoom", equalTo("2"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("sessionStartTime", equalTo(now.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true));
    }
}
