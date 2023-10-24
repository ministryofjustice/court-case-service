package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = {"classpath:sql/before-common.sql", "classpath:sql/before-new-hearing-search.sql"}, config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerPagedCaseListIntTest extends BaseIntTest {

    public static final String KEY_ID = "mock-key";
    private static final String LAST_MODIFIED_COURT_CODE = "B14LO";
    private static final String DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002";

    @Autowired
    ObjectMapper mapper;

    @Autowired
    HearingRepository courtCaseRepository;

    @Autowired
    CourtCaseService courtCaseService;

    private static final LocalDate DECEMBER_14 = LocalDate.of(2019, Month.DECEMBER, 14);
    //2019-12-14
    private static final LocalDate MARCH_25 = LocalDate.of(2022, Month.MARCH, 25);

    private static final LocalDate JAN_1_2010 = LocalDate.of(2010, 1, 1);
    private static final LocalDateTime DECEMBER_14_9AM = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0);
    private static final String CASE_NO = "1600028913";
    private static final String NOT_FOUND_COURT_CODE = "LPL";

    @Test
    void GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases() {

        var url = UriComponentsBuilder.fromPath("/court/{courtCode}/cases")
            .queryParam("VERSION2")
            .queryParam("date", LocalDate.of(2023, 7, 3).format(DateTimeFormatter.ISO_DATE))
                .queryParam("probationStatus", "CURRENT", "Possible NDelius record").build().toUriString();

        final var courtCode = "B14LO";
        given()
            .auth()
            .oauth2(getToken())
            .when()
            .get(url, courtCode)
            .then()
            .assertThat()
            .statusCode(200)
            .body("cases", hasSize(5))
            .body("recentlyAddedCount", equalTo(2))
            .body("possibleMatchesCount", equalTo(2))
            .body("courtRoomFilters", equalTo(List.of("01", "03", "04", "05", "1", "Crown Court 5-1")))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(5))

            .body("cases[0].courtCode", equalTo(courtCode))
            .body("cases[0].caseNo", equalTo("3306014916856309133"))
            .body("cases[0].source", equalTo("LIBRA"))
            .body("cases[0].caseId", equalTo("665597e6-b4d0-466d-98ad-3a9e9eb99b87"))
            .body("cases[0].hearingId", equalTo("1eb3a6da-8189-4de2-8377-da5910e486b9"))
            .body("cases[0].session", equalTo("MORNING"))
            .body("cases[0].defendantType", equalTo("PERSON"))
            .body("cases[0].defendantName", equalTo("Mr Jeff Blogs"))
            .body("cases[0].defendantId", equalTo("8dc4322f-75de-429b-875b-0063b7c0c044"))
            .body("cases[0].probationStatus", equalTo(POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS))
            .body("cases[0].crn", equalTo(null))
            .body("cases[0].offences[0].offenceSummary", equalTo("On 01/01/2016 at Town, stole Article, to the value of £100.00, belonging to Person."))
            .body("cases[0].offences[0].offenceTitle", equalTo("Offence 102"))
            .body("cases[0].offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("cases[0].offences[0].listNo", equalTo(null))

            .body("cases[1].courtCode", equalTo(courtCode))
            .body("cases[1].caseNo", equalTo("3306014916856309133"))
            .body("cases[1].source", equalTo("LIBRA"))
            .body("cases[1].caseId", equalTo("665597e6-b4d0-466d-98ad-3a9e9eb99b87"))
            .body("cases[1].hearingId", equalTo("4a7220b8-88bc-4417-8ee0-cfc318047b3c"))
            .body("cases[1].listNo", is(emptyString()))
            .body("cases[1].session", equalTo("MORNING"))
            .body("cases[1].defendantType", equalTo("PERSON"))
            .body("cases[1].defendantName", equalTo("Mr Jeff Blogs"))
            .body("cases[1].defendantId", equalTo("8dc4322f-75de-429b-875b-0063b7c0c044"))
            .body("cases[1].probationStatus", equalTo(POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS))
            .body("cases[1].crn", equalTo(null))
            .body("cases[1].offences[0].offenceSummary", equalTo("On 01/01/2016 at Town, stole Article, to the value of £100.00, belonging to Person."))
            .body("cases[1].offences[0].offenceTitle", equalTo("Offence 101"))
            .body("cases[1].offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("cases[1].offences[0].listNo", equalTo(null))

            .body("cases[2].courtCode", equalTo(courtCode))
            .body("cases[2].caseNo", equalTo("2307032816883764834"))
            .body("cases[2].source", equalTo("LIBRA"))
            .body("cases[2].caseId", equalTo("cbafcebb-3430-4710-8557-5c93bd1e8be5"))
            .body("cases[2].hearingId", equalTo("cbafcebb-3430-4710-8557-5c93bd1e8be5"))
            .body("cases[2].listNo", equalTo("1st"))
            .body("cases[2].session", equalTo("MORNING"))
            .body("cases[2].defendantType", equalTo("PERSON"))
            .body("cases[2].defendantName", equalTo("Mr Cloud Strife"))
            .body("cases[2].defendantId", equalTo("f984d5bd-ef64-43be-9c03-cc69f94fa2e8"))
            .body("cases[2].probationStatus", equalTo("Current"))
            .body("cases[2].crn", equalTo("X375482"))
            .body("cases[2].offences[0].offenceSummary", equalTo("On 01/01/2016 at Town, stole Article, to the value of £100.00, belonging to Person."))
            .body("cases[2].offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("cases[2].offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))

            .body("cases[3].courtCode", equalTo(courtCode))
            .body("cases[3].caseNo", equalTo("2307032816883764833"))
            .body("cases[3].source", equalTo("LIBRA"))
            .body("cases[3].caseId", equalTo("79c176bf-a6ff-4f82-afba-de136aae1536"))
            .body("cases[3].hearingId", equalTo("79c176bf-a6ff-4f82-afba-de136aae1536"))
            .body("cases[3].listNo", equalTo("1st"))
            .body("cases[3].session", equalTo("MORNING"))
            .body("cases[3].defendantType", equalTo("PERSON"))
            .body("cases[3].defendantName", equalTo("Mr Clifford Li"))
            .body("cases[3].defendantId", equalTo("04ef0041-75eb-4e91-86ea-51703225c6a0"))
            .body("cases[3].probationStatus", equalTo(POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS))
            .body("cases[3].crn", equalTo(null))
            .body("cases[3].offences[0].offenceSummary", equalTo("On 01/01/2016 at Town, stole Article, to the value of £200.00, belonging to Person."))
            .body("cases[3].offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("cases[3].offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))

            .body("cases[4].courtCode", equalTo(courtCode))
            .body("cases[4].caseNo", equalTo(null))
            .body("cases[4].source", equalTo("COMMON_PLATFORM"))
            .body("cases[4].caseId", equalTo("1afb8c07-207c-412d-96ff-1ac5ee7f0b3f"))
            .body("cases[4].hearingId", equalTo("57e86555-bd97-43f7-ad1c-55a992b37a2d"))
            .body("cases[4].listNo", equalTo(null))
            .body("cases[4].session", equalTo("MORNING"))
            .body("cases[4].defendantName", equalTo("Mr Arthur Morgan"))
            .body("cases[4].defendantId", equalTo("7cece15c-78e8-4be9-a509-35d74eb68839"))
            .body("cases[4].probationStatus", equalTo("Current"))
            .body("cases[4].crn", equalTo("X346204"))
            .body("cases[4].offences[0].offenceSummary", equalTo("on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith"))
            .body("cases[4].offences[0].offenceTitle", equalTo("Wound / inflict grievous bodily harm without intent (sole defendant)"))
            .body("cases[4].offences[0].act", equalTo("Contrary to section 20 of the Offences Against the Person Act 1861."))
            .body("cases[4].offences[0].listNo", equalTo(5))
            .body("cases[4].offences[1].offenceSummary", equalTo("on 01/08/2009 at the County public house, unlawfully and maliciously wounded, Jane Smith"))
            .body("cases[4].offences[1].offenceTitle", equalTo("Wound / inflict grievous bodily harm without intent (sole defendant)"))
            .body("cases[4].offences[1].act", equalTo("Contrary to section 20 of the Offences Against the Person Act 1861."))
            .body("cases[4].offences[1].listNo", equalTo(7));
    }
}
