package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvictionRestClientIntTest extends BaseIntTest {

    public static final String CRN = "X320741";
    public static final Long SOME_CONVICTION_ID = 2500295343L;
    public static final Long UNKNOWN_CONVICTION_ID = 9999L;
    public static final String SERVER_ERROR_CRN = "X320500";
    public static final String UNKNOWN_CRN = "CRNXXX";

    @Autowired
    private ConvictionRestClient webTestClient;

    @Test
    void whenGetAttendancesByCrnAndConvictionIdToCommunityApi() {
        final Optional<List<AttendanceResponse>> response = webTestClient.getAttendances(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get()).hasSize(2);
    }

    @Test
    void whenCrnExistsButNoMatchToConvictionIdToCommunityApi() {
        final Optional<List<AttendanceResponse>> response = webTestClient.getAttendances(CRN, UNKNOWN_CONVICTION_ID).blockOptional();

        assertThat(response).hasValueSatisfying(attendancesResponse -> assertThat(attendancesResponse.size()).isEqualTo(0));
    }

    @Test
    void givenServiceThrowsError_whenGetOffenderByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getAttendances(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenServiceThrows400ThenThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getAttendances("XXXXXX", SOME_CONVICTION_ID).blockOptional()
        );
    }

    @Test
    void givenServiceThrows404ThenThrowOffenderNotFoundException() {
        assertThrows(ConvictionNotFoundException.class, () ->
            webTestClient.getAttendances(UNKNOWN_CRN, SOME_CONVICTION_ID).blockOptional()
        );
    }

    @Test
    void whenGetConvictionByCrnAndConvictionIdToCommunityApi() {
        final Optional<Conviction> response = webTestClient.getConviction(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get().getConvictionId()).isEqualTo("2500295343");
    }

    @Test
    void givenServiceThrowsError_whenGetConvictionByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getConviction(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenGetConvictionServiceThrows400ThenThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getConviction("XXXXXX", SOME_CONVICTION_ID).blockOptional()
        );
    }

    @Test
    void givenGetConvictionServiceThrows404ThenThrowOffenderNotFoundException() {
        assertThrows(ConvictionNotFoundException.class, () ->
            webTestClient.getAttendances(UNKNOWN_CRN, SOME_CONVICTION_ID).blockOptional()
        );
    }

    @Test
    void whenGetSentenceStatus_thenReturnIt() {
        final Optional<SentenceStatus> response = webTestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get().mainOffenceDescription()).isEqualTo("Common assault and battery - 10501");
    }

    @Test
    void givenServiceThrowsError_whenGetSentenceStatusByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getSentenceStatus(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenServiceReturns404_whenGetSentenceStatusByCrnCalled_thenReturnDefault() {
        assertThrows(ConvictionNotFoundException.class, () ->
            webTestClient.getSentenceStatus(UNKNOWN_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void whenGetCustodialStatusByCrnAndConvictionIdAndSentenceIdToCommunityApi() {
        final Optional<CustodialStatus> response = webTestClient.getCustodialStatus(CRN, SOME_CONVICTION_ID).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get()).isSameAs(CustodialStatus.POST_SENTENCE_SUPERVISION);
    }

    @Test
    void givenUnknownStatus_whenGetCustodialStatusByCrnAndConvictionId_thenReturn() {
        final Optional<CustodialStatus> response = webTestClient.getCustodialStatus("E396405", 1502992087L).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get()).isSameAs(CustodialStatus.UNKNOWN);
    }

    @Test
    void givenServiceThrowsError_whenGetCustodialStatusByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getCustodialStatus(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenServiceReturns404_whenGetCustodialStatusByCrnCalled_thenReturnDefault() {
        assertThrows(ConvictionNotFoundException.class, () ->
            webTestClient.getCustodialStatus(UNKNOWN_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenKnownValues_whenGetCourtReportsByCrnAndConvictionId_thenReturn() {
        final var response = webTestClient.getCourtReports("X320741", 2500295345L).blockOptional();

        assertThat(response).isPresent();
        assertThat(response.get()).hasSize(2);
    }

    @Test
    void givenServiceThrowsError_whenGetCourtReportsByCrnAndConvictionId_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            webTestClient.getCourtReports(SERVER_ERROR_CRN, SOME_CONVICTION_ID).block()
        );
    }

    @Test
    void givenUnknownCrn_whenGetCourtReportsByCrnAndConvictionId_thenReturn404() {
        assertThrows(ConvictionNotFoundException.class, () ->
            webTestClient.getCourtReports(UNKNOWN_CRN, SOME_CONVICTION_ID).block()
        );
    }
}
