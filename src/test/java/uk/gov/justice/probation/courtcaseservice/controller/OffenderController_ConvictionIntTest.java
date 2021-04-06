package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.time.Month;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.CRN;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_CONVICTION_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_SENTENCE_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.UNKNOWN_CONVICTION_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.UNKNOWN_CRN;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

class OffenderController_ConvictionIntTest extends BaseIntTest {

    private static final String PATH = "/offender/%s/convictions/%s/sentences/%s";
    private static final String CONVICTION_PATH = "/offender/%s/convictions/%s";

    @Autowired
    private FeatureFlags featureFlags;

    @BeforeEach
    void setUp() {
        super.setup();
        featureFlags.setFlagValue("fetch-sentence-data",true);
    }

    @Test
    void whenCallMadeToGetSentenceKnownCrnAndConvictionId() {

        final String getPath = String.format(PATH, CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);
        var response = given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(getPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(SentenceResponse.class);

        assertThat(response.getAttendances()).hasSize(2);

        final AttendanceResponse expectedAttendance1 = AttendanceResponse.builder().contactId(1325L)
            .attendanceDate(LocalDate.of(2019, Month.AUGUST, 24))
            .attended(true).complied(true).outcome("Description of outcome")
            .contactType(ContactTypeDetail.builder().description("Description of contact type").code("DSC01").build()).build();
        final AttendanceResponse expectedAttendance2 = AttendanceResponse.builder().contactId(8888888L)
            .attendanceDate(LocalDate.of(2020, Month.FEBRUARY, 29))
            .attended(false).complied(false).outcome("8888888 Description of outcome")
            .contactType(ContactTypeDetail.builder().description("8888888 Description of contact type").code("DSC02").build()).build();

        assertThat(response.getAttendances()).containsExactlyInAnyOrder(expectedAttendance1, expectedAttendance2);

        assertThat(response.getUnpaidWork())
                .usingRecursiveComparison()
                .isEqualTo(UnpaidWork.builder()
                                                                                            .minutesOffered(3600)
                                                                                            .minutesCompleted(360)
                                                                                            .appointmentsToDate(5)
                                                                                            .attended(2)
                                                                                            .acceptableAbsences(1)
                                                                                            .unacceptableAbsences(1)
                                                                                            .status("Being worked")
                                                                                            .build());

        assertThat(response.getCurrentOrderHeaderDetail())
                .usingRecursiveComparison()
                .isEqualTo(CurrentOrderHeaderResponse.builder()
                    .sentenceId(2500298861L)
                    .custodialType(KeyValue.builder().code("P").description("Post Sentence Supervision").build())
                    .sentenceDescription("CJA - Intermediate Public Prot.")
                    .mainOffenceDescription("Common assault and battery - 10501")
                    .sentenceDate(LocalDate.of(2018, Month.DECEMBER, 3))
                    .actualReleaseDate(LocalDate.of(2019, Month.JULY, 3))
                    .licenceExpiryDate(LocalDate.of(2019, Month.NOVEMBER, 3))
                    .pssEndDate(LocalDate.of(2020, Month.JUNE, 3))
                    .length(11)
                    .lengthUnits("Months")
                    .build());

        assertThat(response.getLinks().getDeliusContactList())
                .isEqualTo("https://ndelius.test.probation.service.justice.gov.uk/NDelius-war/delius/JSP/deeplink.jsp?component=ContactList&offenderId=2500343964&eventId=2500295343");
    }

    @Test
    void whenCallMadeToGetSentenceAttendanceFlagFalseKnownCrnAndConvictionId() {

        featureFlags.setFlagValue("fetch-sentence-data", false);

        final String getPath = String.format(PATH, CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);
        final SentenceResponse response = given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(getPath)
            .then()
                .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(SentenceResponse.class);

        assertThat(response.getCurrentOrderHeaderDetail()).isNull();
        assertThat(response.getAttendances()).isEmpty();
        assertThat(response.getUnpaidWork()).isNotNull();
        assertThat(response.getCurrentOrderHeaderDetail()).isNull();
    }

    @Test
    void whenCallMadeToGetSentenceOnCommunityApiReturns404() {

        final String getPath = String.format(PATH, UNKNOWN_CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
            .get(getPath)
        .then()
            .statusCode(404);
    }

    @Test
    public void whenGetConvictionById_thenReturn() {

        final String path = String.format(CONVICTION_PATH, CRN, SOME_CONVICTION_ID);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("convictionId", equalTo("2500295343"))
            .body("active", equalTo(true))
            .body("inBreach", equalTo(false))
            .body("convictionDate", equalTo("2017-06-01"))
            .body("offences", hasSize(1))
            .body("offences[0].description", equalTo("Arson - 05600"))
            .body("sentence.description", equalTo("CJA - Community Order"))
            .body("sentence.length", equalTo(12))
            .body("sentence.lengthUnits", equalTo("Months"))
            .body("sentence.lengthInDays", equalTo(364))
            .body("sentence.terminationDate", equalTo("2017-12-01"))
            .body("sentence.startDate", equalTo("2017-06-01"))
            .body("sentence.endDate", equalTo("2018-05-31"))
            .body("sentence.terminationReason", equalTo("Completed - early good progress"))
            .body("endDate", equalTo("2018-05-31"))
            .body("breaches", hasSize(2))
            .body("breaches[0].breachId", equalTo(11131322))
            .body("breaches[0].description", equalTo("Community Order"))
            .body("breaches[0].status", equalTo("Breach Initiated"))
            .body("breaches[0].started", equalTo("2020-10-20"))
            .body("breaches[0].statusDate", equalTo("2020-12-18"))
            .body("documents", hasSize(1))
            .body("documents[0].documentId", equalTo("5058ca66-3751-4701-855a-86bf518d9392"))
            .body("documents[0].documentName", equalTo("Event document 2.txt"))
            .body("documents[0].author", equalTo("Andy Marke"))
            .body("documents[0].type", equalTo("COURT_REPORT_DOCUMENT"))
            .body("documents[0].createdAt", equalTo("2019-09-04T00:00:00"))
            .body("documents[0].subType.code", equalTo("CR02"))
            ;
    }

    @Test
    public void givenUnknownConvictionId_whenGetConvictionById_thenReturn404() {

        final String path = String.format(CONVICTION_PATH, CRN, UNKNOWN_CONVICTION_ID);
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("userMessage", equalTo("Conviction with id '9999' for offender with CRN 'X320741' not found"))
            .body("developerMessage" , equalTo("Conviction with id '9999' for offender with CRN 'X320741' not found"))
        ;
    }

}
