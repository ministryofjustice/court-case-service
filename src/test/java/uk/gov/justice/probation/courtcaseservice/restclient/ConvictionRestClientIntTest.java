package uk.gov.justice.probation.courtcaseservice.restclient;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.CurrentOrderHeaderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ConvictionRestClientIntTest {

    public static final String CRN = "X320741";
    public static final Long SOME_CONVICTION_ID = 2500295343L;
    public static final Long SOME_SENTENCE_ID = 2500298861L;
    public static final Long UNKNOWN_CONVICTION_ID = 9999L;
    public static final String SERVER_ERROR_CRN = "X320500";
    public static final String UNKNOWN_CRN = "X320999";

    @Autowired
    private ConvictionRestClient webTestClient;

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .port(WIREMOCK_PORT)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenGetAttendancesByCrnAndConvictionIdToCommunityApi() {
        final Optional<List<AttendanceResponse>> response = webTestClient.getAttendances(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get()).hasSize(2);
    }

    @Test
    public void whenCrnExistsButNoMatchToConvictionIdToCommunityApi() {
        final Optional<List<AttendanceResponse>> response = webTestClient.getAttendances(CRN, UNKNOWN_CONVICTION_ID).blockOptional();

        assertThat(response).hasValueSatisfying(attendancesResponse -> assertThat(attendancesResponse.size() == 0));
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetOffenderByCrnCalled_thenFailFastAndThrowException() {
        webTestClient.getAttendances(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrows400ThenThrowException() {
        webTestClient.getAttendances("XXXXXX", SOME_CONVICTION_ID).blockOptional();
    }

    @Test(expected = ConvictionNotFoundException.class)
    public void givenServiceThrows404ThenThrowOffenderNotFoundException() {
        webTestClient.getAttendances(UNKNOWN_CRN, SOME_CONVICTION_ID).blockOptional();
    }

    @Test
    public void whenGetConvictionByCrnAndConvictionIdToCommunityApi() {
        final Optional<Conviction> response = webTestClient.getConviction(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get().getConvictionId()).isEqualTo("2500295343");
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetConvictionByCrnCalled_thenFailFastAndThrowException() {
        webTestClient.getConviction(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenGetConvictionServiceThrows400ThenThrowException() {
        webTestClient.getConviction("XXXXXX", SOME_CONVICTION_ID).blockOptional();
    }

    @Test(expected = ConvictionNotFoundException.class)
    public void givenGetConvictionServiceThrows404ThenThrowOffenderNotFoundException() {
        webTestClient.getAttendances(UNKNOWN_CRN, SOME_CONVICTION_ID).blockOptional();
    }

    @Test
    public void whenGetCurrentOrderHeaderDetailByCrnAndConvictionIdAndSentenceIdToCommunityApi() {
        final Optional<CurrentOrderHeaderResponse> response = webTestClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID).blockOptional();

        assertThat(response).isPresent();
    }

    @Test(expected = CurrentOrderHeaderNotFoundException.class)
    public void givenServiceThrowsError_whenGetCurrentOrderHeaderByCrnCalled_thenFailFastAndThrowException() {
        webTestClient.getCurrentOrderHeaderDetail(UNKNOWN_CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID).block();
    }

}
