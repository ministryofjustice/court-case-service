package uk.gov.justice.probation.courtcaseservice.controller;


import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.RetryService;
import uk.gov.justice.probation.courtcaseservice.TestConfig;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.time.Month;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.CRN;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_CONVICTION_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_SENTENCE_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.UNKNOWN_CRN;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@RunWith(SpringRunner.class)
@EnableRetry
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderController_ConvictionIntTest {

    private static final String PATH = "/offender/%s/convictions/%s/sentences/%s";

    @Autowired
    private FeatureFlags featureFlags;

    @LocalServerPort
    private int port;

    @BeforeClass
    public static void setupClass() throws Exception {
        RetryService.tryWireMockStub();
    }

    @Before
    public void setUp() {
        featureFlags.setFlagValue("fetch-sentence-data",true);
        TestConfig.configureRestAssuredForIntTest(port);
    }

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
        .port(WIREMOCK_PORT)
        .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenCallMadeToGetSentenceKnownCrnAndConvictionId() {

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

        assertThat(response.getUnpaidWork()).isEqualToComparingFieldByField(UnpaidWork.builder()
                                                                                            .minutesOffered(3600)
                                                                                            .minutesCompleted(360)
                                                                                            .appointmentsToDate(5)
                                                                                            .attended(2)
                                                                                            .acceptableAbsences(1)
                                                                                            .unacceptableAbsences(1)
                                                                                            .status("Being worked")
                                                                                            .build());

        assertThat(response.getCurrentOrderHeaderDetail()).isEqualToComparingFieldByField(CurrentOrderHeaderResponse.builder()
                .sentenceId(2500298861L)
                .custodialType(KeyValue.builder().code("P").description("Post Sentence Supervision").build())
                .sentenceDescription("CJA - Intermediate Public Prot.")
                .mainOffenceDescription("Common assault and battery - 10501")
                .sentenceDate(LocalDate.of(2018, Month.DECEMBER, 03))
                .actualReleaseDate(LocalDate.of(2019, Month.JULY, 03))
                .licenceExpiryDate(LocalDate.of(2019, Month.NOVEMBER, 03))
                .pssEndDate(LocalDate.of(2020, Month.JUNE, 03))
                .length(11)
                .lengthUnits("Months")
                .build());
    }

    @Test
    public void whenCallMadeToGetSentenceAttendanceFlagFalseKnownCrnAndConvictionId() {

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
    public void whenCallMadeToGetSentenceOnCommunityApiReturns404() {

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
