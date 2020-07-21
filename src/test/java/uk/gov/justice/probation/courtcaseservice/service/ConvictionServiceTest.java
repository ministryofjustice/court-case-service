package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvictionServiceTest {

    public static final String CRN = "X370652";
    public static final Long SOME_CONVICTION_ID = 1234L;
    public static final Long SOME_SENTENCE_ID = 5467L;
    private Conviction conviction;

    @Mock
    private List<AttendanceResponse> attendancesResponse;

    @Mock
    private CurrentOrderHeaderResponse currentOrderHeaderResponse;

    @Mock
    private ConvictionRestClient restClient;

    @InjectMocks
    private ConvictionService service;

    @BeforeEach
    void beforeEach() {
        final Sentence sentence = Sentence.builder().unpaidWork(UnpaidWork.builder().acceptableAbsences(100).build()).build();
        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).sentence(sentence).build();
    }

    @DisplayName("Normal retrieval of conviction with unpaid work")
    @Test
    public void givenConviction_whenGetConviction_returnConviction() {

        when(restClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(restClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(restClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);
        verify(restClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(restClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(restClient);
    }

    @DisplayName("Retrieval of conviction with null sentence and therefore no unpaid work")
    @Test
    public void givenNullSentenceOnConviction_whenGetSentence_returnSentenceNoUnPaidWork() {

        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).build();
        when(restClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(restClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(restClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork()).isNull();
        verify(restClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(restClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(restClient);
    }

    @DisplayName("Retrieval of conviction with empty list of attendances")
    @Test
    public void givenNoAttendances_whenGetConviction_returnConviction() {

        when(restClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
        when(restClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(restClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response.getAttendances()).isEmpty();
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);
        verify(restClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(restClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(restClient);
    }

    @DisplayName("An error response means that we get an exception")
    @Test
    void givenAttendancesNotFound_whenGetAttendances_thenThrowException() {

        when(restClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(restClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(restClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

        assertThatExceptionOfType(OffenderNotFoundException.class)
            .isThrownBy(() -> service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID))
            .withMessageContaining(CRN);
    }

    @DisplayName("Successful retrieval of current order header detail")
    @Test
    public void givenCurrentOrderHeaderDetail_whenGetCurrentOrderHeaderDetail_returnCurrentOrderHeaderDetail() {

        when(restClient.getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));

        final CurrentOrderHeaderResponse response = service.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response).isEqualTo(currentOrderHeaderResponse);
        verify(restClient).getCurrentOrderHeaderDetail(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);
    }
}
