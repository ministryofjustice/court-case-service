package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static java.time.Month.JANUARY;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerPutIntTest extends BaseIntTest {
    public static final String CRO = "CRO";

    /* before-test.sql sets up a court case in the database */

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String CRN = "X320741";
    private static final String PNC = "A/1234560BA";
    private static final String COURT_ROOM = "1";
    private static final String LIST_NO = "1st";
    private static final String DEFENDANT_SEX = "M";
    private static final String NATIONALITY_1 = "British";
    private static final String NATIONALITY_2 = "Polish";
    private static final String COURT_CODE = "SHF";
    private static final AddressPropertiesEntity ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "ad21 5dr", "Bangor", null, null);
    private static final String PROBATION_STATUS = "Previously known";
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private static final String DEFENDANT_NAME = "JTEST";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 12, 14);
    private static final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14, 9, 0);

    @Value("classpath:integration/request/PUT_courtCase_success.json")
    private Resource caseDetailsResource;
    private String caseDetailsJson;

    /** NEW values match those from JSON file incoming. */
    private static final String JSON_CASE_NO = "1700028914";
    private static final String JSON_CASE_ID = "654321";

    @Before
    public void setup() throws Exception {
        caseDetailsJson = Files.readString(caseDetailsResource.getFile().toPath());
        super.setup();
    }

    @Test
    public void whenCreateCaseDataByCourtAndCaseNo_ThenCreateNewRecord() {

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseDetailsJson)
        .when()
            .put(String.format("/court/%s/case/%s", COURT_CODE, JSON_CASE_NO))
        .then()
            .statusCode(201)
            .body("caseId", equalTo(JSON_CASE_ID))
            .body("caseNo", equalTo(JSON_CASE_NO))
            .body("crn", equalTo(CRN))
            .body("courtCode", equalTo(COURT_CODE))
            .body("courtRoom", equalTo(COURT_ROOM))
            .body("probationStatus", equalTo(PROBATION_STATUS))
            .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
            .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("suspendedSentenceOrder", equalTo(true))
            .body("breach", equalTo(true))
            .body("defendantName", equalTo(DEFENDANT_NAME))
            .body("defendantAddress.line1", equalTo(ADDRESS.getLine1()))
            .body("defendantAddress.line2", equalTo(ADDRESS.getLine2()))
            .body("defendantAddress.postcode", equalTo(ADDRESS.getPostcode()))
            .body("defendantAddress.line3", equalTo(ADDRESS.getLine3()))
            .body("defendantAddress.line4", equalTo(null))
            .body("defendantAddress.line5", equalTo(null))
            .body("offences", hasSize(2))
            .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
            .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("offences[0]", not(hasKey("courtCase")))
            .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
            .body("pnc", equalTo(PNC))
            .body("listNo", equalTo(LIST_NO))
            .body("defendantDob", equalTo(LocalDate.of(1958, 12, 14).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("defendantSex", equalTo(DEFENDANT_SEX))
            .body("nationality1", equalTo(NATIONALITY_1))
            .body("nationality2", equalTo(NATIONALITY_2))
        ;

    }

    @Test
    public void whenUpdateCaseDataByCourtAndCaseNo_ThenUpdate() {

        createCase();

        String updatedJson = caseDetailsJson.replace("\"courtRoom\": \"1\"", "\"courtRoom\": \"2\"");

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(updatedJson)
        .when()
            .put(String.format("/court/%s/case/%s", COURT_CODE, JSON_CASE_NO))
        .then()
            .statusCode(201)
            .body("caseId", equalTo(JSON_CASE_ID))
            .body("caseNo", equalTo(JSON_CASE_NO))
            .body("courtCode", equalTo(COURT_CODE))
            .body("crn", equalTo(CRN))
            .body("pnc", equalTo(PNC))
            .body("listNo", equalTo(LIST_NO))
            .body("defendantDob", equalTo(LocalDate.of(1958, 12, 14).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("defendantSex", equalTo(DEFENDANT_SEX))
            .body("nationality1", equalTo(NATIONALITY_1))
            .body("nationality2", equalTo(NATIONALITY_2))
            .body("courtRoom", equalTo("2"))
            .body("probationStatus", equalTo(PROBATION_STATUS))
            .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
            .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2018, 6, 24).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("suspendedSentenceOrder", equalTo(true))
            .body("breach", equalTo(true))
            .body("defendantName", equalTo(DEFENDANT_NAME))
            .body("defendantAddress.line1", equalTo(ADDRESS.getLine1()))
            .body("defendantAddress.line2", equalTo(ADDRESS.getLine2()))
            .body("defendantAddress.postcode", equalTo(ADDRESS.getPostcode()))
            .body("defendantAddress.line3", equalTo(ADDRESS.getLine3()))
            .body("defendantAddress.line4", equalTo(null))
            .body("defendantAddress.line5", equalTo(null))
            .body("offences", hasSize(2))
            .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
            .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
        ;

    }

    @Test
    public void whenUpdateCaseDataByCourtAndCaseNo_ThenUpdateOffenderMatchesConfirmedRejectedFlags() {

        String updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"1600028913\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"2234\"")
                .replace("\"pnc\": \"A/1234560BA\"", "\"pnc\": \"223456\"")
                .replace("\"cro\": \"99999\"", "\"cro\": \"22345\"")
                ;

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
            .when()
                .put(String.format("/court/%s/case/%s", COURT_CODE, "1600028913"))
            .then()
                .statusCode(201)
                .body("caseNo", equalTo("1600028913"))
                .body("crn", equalTo("2234"))
                .body("pnc", equalTo("223456"))
                .body("cro", equalTo("22345"));

        given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
            .when()
                .get("/court/SHF/case/1600028913/grouped-offender-matches/9999991")
            .then()
                .statusCode(200)
                .body("offenderMatches[0].crn", equalTo("1234"))
                .body("offenderMatches[0].confirmed",  equalTo(false))
                .body("offenderMatches[0].rejected",  equalTo(true))
                .body("offenderMatches[1].crn", equalTo("2234"))
                .body("offenderMatches[1].confirmed",  equalTo(true))
                .body("offenderMatches[1].rejected",  equalTo(false))
                ;

        given()
                .auth()
                .oauth2(getToken())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
            .when()
                .get("/court/SHF/case/1600028913/grouped-offender-matches/9999992")
            .then()
                .statusCode(200)
                .body("offenderMatches[0].crn", equalTo("3234"))
                .body("offenderMatches[0].confirmed",  equalTo(false))
                .body("offenderMatches[0].rejected",  equalTo(true))
                ;
    }

    @Test
    public void whenCreateCourtCaseByCourtAndCaseWithUnknownCourt_ThenRaise404() {

        CourtCaseEntity courtCaseEntity = createCaseDetails(NOT_FOUND_COURT_CODE, JSON_CASE_NO, JSON_CASE_ID);

        ErrorResponse result = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(courtCaseEntity)
            .when()
            .put(String.format("/court/%s/case/%s", NOT_FOUND_COURT_CODE, JSON_CASE_NO))
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
    public void whenCreateCourtCaseByCourtAndCaseWithMismatchCourt_ThenRaise500() {

        CourtCaseEntity courtCaseEntity = createCaseDetails(COURT_CODE, JSON_CASE_NO, JSON_CASE_ID);

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(courtCaseEntity)
            .when()
            .put(String.format("/court/%s/case/%s", "NWS", "99999"))
            .then()
            .statusCode(500)
            .body("message", equalTo("Case No 99999 and Court Code NWS do not match with values from body 1700028914 and SHF"))
        ;

    }

    @Test
    public void whenPurgeCases_ThenReturn204NoContent() {

        LocalDate date1Jan = LocalDate.of(2020, JANUARY, 1);
        LocalDate date2Jan = LocalDate.of(2020, JANUARY, 2);
        LocalDate date3Jan = LocalDate.of(2020, JANUARY, 3);

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(Map.of(date1Jan, Arrays.asList("1000000", "1000001"), date2Jan, Arrays.asList("1000003", "1000007"), date3Jan, singletonList("1000010")))
            .when()
            .put(String.format("/court/%s/cases/purgeAbsent", COURT_CODE))
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
        ;

        // 1 Jan - 2 cases - neither deleted
        LocalDateTime start = LocalDateTime.of(date1Jan, LocalTime.MIDNIGHT);
        List<CourtCaseEntity> courtCases1 = courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(COURT_CODE, start, start.plusDays(1));
        assertThat(courtCases1).hasSize(2);
        assertThat(courtCases1).extracting("deleted").containsOnly(Boolean.FALSE);

        // 2 Jan - 6 cases - 4 removed and deleted also on child offences
        start = LocalDateTime.of(date2Jan, LocalTime.MIDNIGHT);
        List<CourtCaseEntity> courtCases2 = courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(COURT_CODE, start, start.plusDays(1));
        assertThat(courtCases2).hasSize(6);
        List<String> date2Deleted = courtCases2.stream()
            .filter(BaseEntity::isDeleted)
            .map(CourtCaseEntity::getCaseNo)
            .collect(Collectors.toList());
        assertThat(date2Deleted).containsAll(Arrays.asList("1000002", "1000005", "1000005", "1000006"));
        List<OffenceEntity> deletedCaseWithOffences = courtCases2.stream()
            .filter(e -> e.isDeleted() && !e.getOffences().isEmpty())
            .findFirst()
            .map(CourtCaseEntity::getOffences)
            .orElseThrow();
        assertThat(deletedCaseWithOffences).extracting("deleted").containsOnly(Boolean.TRUE);

        // 3 Jan - all 2 removed
        start = LocalDateTime.of(date3Jan, LocalTime.MIDNIGHT);
        List<CourtCaseEntity> courtCases3 = courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(COURT_CODE, start, start.plusDays(1));
        assertThat(courtCases3).hasSize(2);
        assertThat(courtCases3).extracting("deleted").containsOnly(Boolean.TRUE);
    }

    @Test
    public void givenUnknownCourtCode_whenPurgeCases_ThenReturn404() {

        Map<LocalDate, List<Long>> existingCases = new HashMap<>(2);
        existingCases.put(LocalDate.now(), Arrays.asList(1L, 4L));

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(existingCases)
            .when()
            .put(String.format("/court/%s/cases/purge", "XXX"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
        ;
    }

    private CourtCaseEntity createCaseDetails(String courtCode, String caseNo, String caseId) {

        CourtCaseEntity caseDetails = new CourtCaseEntity();
        caseDetails.setCourtCode(courtCode);
        caseDetails.setCaseNo(caseNo);
        caseDetails.setCaseId(caseId);
        caseDetails.setCourtRoom("1");
        caseDetails.setSessionStartTime(LocalDateTime.now());
        caseDetails.setProbationStatus(PROBATION_STATUS);
        caseDetails.setLastUpdated(LocalDateTime.now());
        caseDetails.setPreviouslyKnownTerminationDate(LocalDate.of(2010, 1, 1));
        caseDetails.setSuspendedSentenceOrder(true);
        caseDetails.setBreach(true);
        caseDetails.setDefendantName(DEFENDANT_NAME);
        caseDetails.setDefendantAddress(ADDRESS);
        caseDetails.setPnc(PNC);
        caseDetails.setListNo(LIST_NO);
        caseDetails.setDefendantDob(DEFENDANT_DOB);
        caseDetails.setDefendantSex(DEFENDANT_SEX);
        caseDetails.setNationality1(NATIONALITY_1);
        caseDetails.setNationality2(NATIONALITY_2);
        return caseDetails;
    }

    private void createCase() {

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseDetailsJson)
            .when()
            .put(String.format("/court/%s/case/%s", COURT_CODE, JSON_CASE_NO))
            .then()
            .statusCode(201)
            .body("caseId", equalTo(JSON_CASE_ID))
            .body("caseNo", equalTo(JSON_CASE_NO))
            .body("crn", equalTo(CRN))
            .body("courtCode", equalTo(COURT_CODE))
        ;
    }
}
