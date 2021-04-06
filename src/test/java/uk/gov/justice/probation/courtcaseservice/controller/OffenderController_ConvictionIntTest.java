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
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.CRN;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_CONVICTION_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_SENTENCE_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.UNKNOWN_CRN;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

class OffenderController_ConvictionIntTest extends BaseIntTest {

    private static final String PATH = "/offender/%s/convictions/%s/sentences/%s";

    @Autowired
    private FeatureFlags featureFlags;

    @BeforeEach
    void setUp() throws Exception {
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
}
