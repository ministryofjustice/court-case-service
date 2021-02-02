package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

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
    public static final Long SOME_SENTENCE_ID = 5467L;
    public static final Long SOME_OFFENDER_ID = 789456L;
    private static final String PSS_DESC_TO_KEEP = "specified activity";
    private static final OffenderDetail OFFENDER_DETAIL = OffenderDetail.builder()
            .otherIds(OtherIds.builder()
                    .offenderId(SOME_OFFENDER_ID)
                    .build())
            .build();
    private static final String DELIUS_LINK_TEMPLATE = "http://test.url/foo/?bar=%s&baz=%s";
    private Conviction conviction;

    @Mock
    private List<Requirement> expectedRequirements;

    @Mock
    private List<AttendanceResponse> attendancesResponse;

    @Mock
    private CurrentOrderHeaderResponse currentOrderHeaderResponse;

    @Mock
    private ConvictionRestClient convictionRestClient;

    @Mock
    private OffenderRestClientFactory offenderRestClientFactory;

    @Mock
    private OffenderRestClient offenderRestClient;

    private ConvictionService service;

    @BeforeEach
    void beforeEach() {
        when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
        service = new ConvictionService(convictionRestClient, offenderRestClientFactory, DELIUS_LINK_TEMPLATE);
        service.setPssRqmntDescriptionsKeepSubType(List.of(PSS_DESC_TO_KEEP));
        final Sentence sentence = Sentence.builder().unpaidWork(UnpaidWork.builder().acceptableAbsences(100).build()).build();
        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).sentence(sentence).build();
    }

    @DisplayName("Normal retrieval of conviction with unpaid work")
    @Test
    public void givenConviction_whenGetConviction_returnConviction() {

        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));
        when(offenderRestClient.getOffenderDetailByCrn(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork().getAcceptableAbsences()).isEqualTo(100);
        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("Retrieval of conviction with null sentence and therefore no unpaid work")
    @Test
    public void givenNullSentenceOnConviction_whenGetSentence_returnSentenceNoUnPaidWork() {

        conviction = Conviction.builder().convictionId(String.valueOf(SOME_CONVICTION_ID)).build();
        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));
        when(offenderRestClient.getOffenderDetailByCrn(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

        assertThat(response.getLinks().getDeliusContactList()).isEqualTo(String.format(DELIUS_LINK_TEMPLATE, SOME_OFFENDER_ID, SOME_CONVICTION_ID));
        assertThat(response.getAttendances()).isSameAs(attendancesResponse);
        assertThat(response.getUnpaidWork()).isNull();
        verify(convictionRestClient).getAttendances(CRN, SOME_CONVICTION_ID);
        verify(convictionRestClient).getConviction(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(convictionRestClient);
    }

    @DisplayName("Retrieval of conviction with empty list of attendances")
    @Test
    public void givenNoAttendances_whenGetConviction_returnConviction() {

        when(convictionRestClient.getAttendances(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
        when(convictionRestClient.getConviction(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(convictionRestClient.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));
        when(offenderRestClient.getOffenderDetailByCrn(CRN)).thenReturn(Mono.just(OFFENDER_DETAIL));

        final SentenceResponse response = service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID);

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
        when(convictionRestClient.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(offenderRestClient.getOffenderDetailByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

        assertThatExceptionOfType(OffenderNotFoundException.class)
            .isThrownBy(() -> service.getSentence(CRN, SOME_CONVICTION_ID, SOME_SENTENCE_ID))
            .withMessageContaining(CRN);
    }

    @DisplayName("Successful retrieval of current order header detail")
    @Test
    public void givenCurrentOrderHeaderDetail_whenGetCurrentOrderHeaderDetail_returnCurrentOrderHeaderDetail() {

        when(convictionRestClient.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(currentOrderHeaderResponse));

        final CurrentOrderHeaderResponse response = service.getCurrentOrderHeader(CRN, SOME_CONVICTION_ID);

        assertThat(response).isEqualTo(currentOrderHeaderResponse);
        verify(convictionRestClient).getCurrentOrderHeader(CRN, SOME_CONVICTION_ID);
    }

    @DisplayName("Conviction requirements for offender on licence, do not return PSS, filter out inactive and remove subtype descriptions")
    @Test
    void givenStatusOnLicence_whenGetConvictionRequirements_returnRequirementsWithNoPss() {

        var pssRqmnt1 = PssRequirement.builder()
            .active(true)
            .description(PSS_DESC_TO_KEEP)
            .subTypeDescription("subType desc 1")
            .build();

        var licenceCondition1 = LicenceCondition.builder().description("Desc 1").active(false).build();
        var licenceCondition2 = LicenceCondition.builder().description("Desc 2").active(true).build();

        when(offenderRestClient.getConvictionRequirements(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(expectedRequirements));
        when(offenderRestClient.getConvictionPssRequirements(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(List.of(pssRqmnt1)));
        when(offenderRestClient.getConvictionLicenceConditions(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(List.of(licenceCondition1, licenceCondition2)));
        when(convictionRestClient.getCustodialStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(CustodialStatus.RELEASED_ON_LICENCE));

        var requirements = service.getConvictionRequirements(CRN, SOME_CONVICTION_ID).block();

        assertThat(requirements.getRequirements()).isSameAs(expectedRequirements);
        assertThat(requirements.getPssRequirements()).isEmpty();
        assertThat(requirements.getLicenceConditions()).hasSize(1);
        assertThat(requirements.getLicenceConditions().get(0).getDescription()).isEqualTo("Desc 2");
    }

    @DisplayName("Conviction requirements for offender on PSS, do not return licence conditions, filter out inactive and remove subtype descriptions")
    @Test
    void givenStatusPSS_whenGetConvictionRequirements_returnRequirementsWithNoLicencePresent() {

        var pssRqmnt1 = PssRequirement.builder()
            .active(true)
            .description(PSS_DESC_TO_KEEP)
            .subTypeDescription("subType desc 1")
            .build();
        var pssRqmnt2 = PssRequirement.builder()
            .active(false)
            .description("Desc rqmnt 2")
            .subTypeDescription("subType desc 2")
            .build();
        var pssRqmnt3 = PssRequirement.builder()
            .active(true)
            .description("Desc rqmnt 3")
            .subTypeDescription("subType desc 3")
            .build();

        var licenceCondition1 = LicenceCondition.builder().description("Desc 2").active(true).build();

        when(offenderRestClient.getConvictionRequirements(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(expectedRequirements));
        when(offenderRestClient.getConvictionPssRequirements(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(List.of(pssRqmnt1, pssRqmnt2, pssRqmnt3)));
        when(offenderRestClient.getConvictionLicenceConditions(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(List.of(licenceCondition1)));
        when(convictionRestClient.getCustodialStatus(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(CustodialStatus.POST_SENTENCE_SUPERVISION));

        var requirements = service.getConvictionRequirements(CRN, SOME_CONVICTION_ID).block();

        assertThat(requirements.getRequirements()).isSameAs(expectedRequirements);
        assertThat(requirements.getPssRequirements()).hasSize(2);
        assertThat(requirements.getPssRequirements()).extracting("description").containsExactly(PSS_DESC_TO_KEEP, "Desc rqmnt 3");
        assertThat(requirements.getLicenceConditions()).isEmpty();
    }

}
