package uk.gov.justice.probation.courtcaseservice.controller;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.CRN;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.SOME_CONVICTION_ID;
import static uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClientIntTest.UNKNOWN_CRN;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.ConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class ConvictionControllerIntTest {

    private static final String PATH = "/offenders/%s/convictions/%s";

    @Autowired
    private FeatureFlags featureFlags;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        featureFlags.setFlagValue("fetch-attendance-data",true);
        TestConfig.configureRestAssuredForIntTest(port);
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
        .port(8090)
        .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenCallMadeToGetConvictionKnownCrnAndConvictionId() {

        final String getPath = String.format(PATH, CRN, SOME_CONVICTION_ID);
        final ConvictionResponse response = given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(getPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(ConvictionResponse.class);

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
                                                                                            .unacceptableAbsences(1));
    }

    @Test
    public void whenCallMadeToGeConvictionAttendanceFlagFalseKnownCrnAndConvictionId() {

        featureFlags.setFlagValue("fetch-attendance-data", false);

        final String getPath = String.format(PATH, CRN, SOME_CONVICTION_ID);
        final ConvictionResponse response = given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(getPath)
            .then()
                .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(ConvictionResponse.class);

        assertThat(response.getAttendances()).isEmpty();
        assertThat(response.getUnpaidWork()).isNotNull();
    }

    @Test
    public void whenCallMadeToGetConvictionOnCommunityApiReturns404() {

        final String getPath = String.format(PATH, UNKNOWN_CRN, SOME_CONVICTION_ID);
        given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(getPath)
        .then()
            .statusCode(404);
    }
}
