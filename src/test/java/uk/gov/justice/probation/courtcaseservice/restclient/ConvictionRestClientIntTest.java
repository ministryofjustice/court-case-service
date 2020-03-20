package uk.gov.justice.probation.courtcaseservice.restclient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ConvictionRestClientIntTest {

    public static final String CRN = "X320741";
    public static final Long SOME_CONVICTION_ID = 1234L;
    public static final Long UNKNOWN_CONVICTION_ID = 9999L;
    public static final String SERVER_ERROR_CRN = "X320500";
    public static final String UNKNOWN_CRN = "X320999";

    @Autowired
    private ConvictionRestClient webTestClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenGetAttendancesByCrnAndConvictionIdToCommunityApi() {
        final Optional<AttendancesResponse> response = webTestClient.getAttendancesByCrnAndConvictionId(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).hasValueSatisfying(attendancesResponse -> {
            assertThat(CRN).isEqualTo(attendancesResponse.getCrn());
            assertThat(attendancesResponse.getConvictionId().longValue() == SOME_CONVICTION_ID.longValue());
            assertThat(attendancesResponse.getAttendances().size() == 2);
        });
    }

    @Test
    public void whenCrnExistsButNoMatchToConvictionIdToCommunityApi() {
        final Optional<AttendancesResponse> response = webTestClient.getAttendancesByCrnAndConvictionId(CRN, UNKNOWN_CONVICTION_ID).blockOptional();

        assertThat(response).hasValueSatisfying(attendancesResponse -> {
            assertThat(CRN).isEqualTo(attendancesResponse.getCrn());
            assertThat(attendancesResponse.getConvictionId().longValue() == UNKNOWN_CONVICTION_ID.longValue());
            assertThat(attendancesResponse.getAttendances().size() == 0);
        });
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetOffenderByCrnCalled_thenFailFastAndThrowException() {
        webTestClient.getAttendancesByCrnAndConvictionId(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block();
    }

    @Ignore
    @Test
    public void givenUnknown404Return404() {
        final Optional<AttendancesResponse> response = webTestClient.getAttendancesByCrnAndConvictionId(UNKNOWN_CRN, SOME_CONVICTION_ID)
                                                                    .blockOptional();

        assertThat(response).isEmpty();
    }
}
