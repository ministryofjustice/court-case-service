package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvictionServiceTest {

    public static final String CRN = "X370652";
    public static final Long SOME_CONVICTION_ID = 1234L;
    public static final Long SOME_OFFENDER_ID = 789456L;
    private static final CommunityApiOffenderResponse OFFENDER_DETAIL = CommunityApiOffenderResponse.builder()
            .offenderId(SOME_OFFENDER_ID)
            .build();
    private static final String DELIUS_LINK_TEMPLATE = "http://test.url/foo/?bar=%s&baz=%s";
    private Conviction conviction;

    private SentenceStatus sentenceStatusResponse;

    @Mock
    private List<AttendanceResponse> attendancesResponse;

    @Mock
    private ConvictionRestClient convictionRestClient;

    @Mock
    private OffenderRestClientFactory offenderRestClientFactory;

    @Mock
    private OffenderRestClient offenderRestClient;

    private ConvictionService service;

    @BeforeEach
    void beforeEach() {
        sentenceStatusResponse = SentenceStatus.builder()
            .custodialType(KeyValue.builder().code("P").description("PSS").build())
            .sentenceId(1L)
            .length(1)
            .lengthUnits("months")
            .actualReleaseDate(LocalDate.of(2019, 10, 1))
            .pssEndDate(LocalDate.of(2019, 11, 1))
            .mainOffenceDescription("main offence")
            .sentenceDate(LocalDate.of(2018, 10, 1))
            .sentenceDescription("sent down")
            .build();
        final Sentence sentence = Sentence.builder().unpaidWork(UnpaidWork.builder().acceptableAbsences(100).build()).build();
        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).sentence(sentence).build();

        when(offenderRestClientFactory.buildUserAwareOffenderRestClient()).thenReturn(offenderRestClient);
        service = new ConvictionService(convictionRestClient, offenderRestClientFactory, DELIUS_LINK_TEMPLATE);
    }

    @DisplayName("Normal retrieval of sentence with unpaid work, attendances and links")
    @Test
    void givenCustodialOrderFlag_whenGetSentence_return() {

        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(sentenceStatusResponse));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);

        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("Normal retrieval of full sentence with attendances, links, unpaid work")
    @Test
    void whenGetSentence_returnSentence() {
        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(sentenceStatusResponse));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);
        assertThat(response.getSentenceId()).isEqualTo(1L);
        assertThat(response.getCustody().getCustodialType().getCode()).isEqualTo("P");
        assertThat(response.getCustody().getCustodialType().getDescription()).isEqualTo("PSS");
        assertThat(response.getCustody().getPssEndDate()).isEqualTo(LocalDate.of(2019, 11, 1));
        assertThat(response.getCustody().getLicenceExpiryDate()).isNull();
        assertThat(response.getLength()).isEqualTo(1);
        assertThat(response.getLengthUnits()).isEqualTo("months");
        assertThat(response.getActualReleaseDate()).isEqualTo(LocalDate.of(2019, 10, 1));
        assertThat(response.getMainOffenceDescription()).isEqualTo("main offence");
        assertThat(response.getSentenceDescription()).isEqualTo("sent down");
        assertThat(response.getSentenceDate()).isEqualTo(LocalDate.of(2018, 10, 1));

        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("Retrieval of conviction with null sentence and therefore no unpaid work")
    @Test
    void givenNullSentenceOnConviction_whenGetSentence_returnSentenceNoUnPaidWork() {

        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).build();
        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(sentenceStatusResponse));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork()).isNull();
        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("Retrieval of sentence with empty list of attendances")
    @Test
    void givenNoAttendances_whenGetSentence_return() {

        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(sentenceStatusResponse));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isEmpty();
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);
        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("An error response means that we get an exception")
    @Test
    void givenAttendancesNotFound_whenGetAttendances_thenThrowException() {

        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

        assertThatExceptionOfType(OffenderNotFoundException.class)
            .isThrownBy(() -> service.getSentence(CRN, SOME_CONVICTION_ID))
            .withMessageContaining(CRN);
    }

    @DisplayName("Successful retrieval of current order header detail")
    @Test
    void whenGetSentenceStatus_returnIt() {

        when(convictionRestClient.getSentenceStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(sentenceStatusResponse));

        final SentenceStatus response = service.getSentenceStatus(CRN, SOME_CONVICTION_ID);

        assertThat(response).isEqualTo(sentenceStatusResponse);
        verify(convictionRestClient).getSentenceStatus(CRN, SOME_CONVICTION_ID);
    }

}
