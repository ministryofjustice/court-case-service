package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.service.BreachService;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;
import uk.gov.justice.probation.courtcaseservice.service.CustodyService;
import uk.gov.justice.probation.courtcaseservice.service.DocumentService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderControllerTest {
    private static final String CRN = "CRN";
    private static final String CONVICTION_ID = "CONVICTION_ID";
    static final Long SOME_EVENT_ID = 1234L;
    static final Long SOME_SENTENCE_ID = 1234L;

    @Mock
    private DocumentService documentService;
    @Mock
    private OffenderService offenderService;
    @Mock
    private ProbationRecord expectedProbationRecord;
    @Mock
    private BreachResponse expectedBreach;
    @Mock
    private ConvictionService convictionService;
    @Mock
    private BreachService breachService;
    @Mock
    private CustodyService custodyService;

    private FeatureFlags featureFlags;

    private OffenderController controller;
    public static final long BREACH_CONVICTION_ID = 123455L;
    public static final long BREACH_ID = 1234456L;

    @BeforeEach
    void beforeEach() {
        featureFlags = new FeatureFlags();
        controller = new OffenderController(offenderService, convictionService, breachService, featureFlags, documentService, custodyService);
    }

    @DisplayName("Normal sentence service call returns response")
    @Test
    void callReturnsResponse() {
        var attendancesResponse = SentenceResponse.builder().attendances(Collections.emptyList()).build();
        when(convictionService.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).thenReturn(attendancesResponse);

        assertThat(controller.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).isEqualTo(attendancesResponse);

        verify(convictionService).getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Feature toggle for sentence data is off")
    @Test
    void featureToggleFalseSentenceData() {
        featureFlags.setFlagValue("fetch-sentence-data", false);
        var sentenceResponse = SentenceResponse.builder()
                .attendances(Collections.emptyList())
                .unpaidWork(UnpaidWork.builder().build())
                .build();
        when(convictionService.getConvictionOnly(CRN, SOME_EVENT_ID)).thenReturn(sentenceResponse);

        assertThat(controller.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).usingRecursiveComparison().isEqualTo(sentenceResponse);
        verify(convictionService).getConvictionOnly(CRN, SOME_EVENT_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same offender probation record")
    @Test
    public void whenGetProbationRecord_thenReturnIt() {

        var applyFilter = true;
        when(offenderService.getProbationRecord(CRN, applyFilter)).thenReturn(expectedProbationRecord);

        var probationRecordResponse = controller.getProbationRecord(CRN, applyFilter);

        assertThat(probationRecordResponse).isNotNull();
        assertThat(probationRecordResponse).isEqualTo(expectedProbationRecord);
        verify(offenderService).getProbationRecord(CRN, applyFilter);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same breach")
    @Test
    public void whenGetBreach_thenReturnIt() {

        when(breachService.getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID)).thenReturn(expectedBreach);

        var actualBreach = controller.getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID);

        assertThat(actualBreach).isNotNull();
        assertThat(actualBreach).isEqualTo(expectedBreach);
        verify(breachService).getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID);
    }

    @DisplayName("Ensures that the controller calls the document service and returns the same document")
    @Test
    public void whenGetDocument_thenReturnIt() {

        var expectedResponse = mock(ResponseEntity.class);
        when(documentService.getDocument(CRN, CONVICTION_ID)).thenReturn(expectedResponse);

        var responseEntity = controller.getOffenderDocumentByCrn(CRN, CONVICTION_ID);

        assertThat(responseEntity).isSameAs(expectedResponse);
        verify(documentService).getDocument(CRN, CONVICTION_ID);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same offender detail record")
    @Test
    public void whenGetOffenderDetail_thenReturnIt() {
        var offenderDetail = mock(OffenderDetail.class);

        when(offenderService.getOffenderDetail(CRN)).thenReturn(Mono.just(offenderDetail));

        var offenderDetailResponse = controller.getOffenderDetail(CRN).block();

        assertThat(offenderDetailResponse).isSameAs(offenderDetail);
        verify(offenderService).getOffenderDetail(CRN);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same registrations")
    @Test
    public void whenGetOffenderRegistrations_thenReturnIt() {
        var registration = Registration.builder().build();
        when(offenderService.getOffenderRegistrations(CRN)).thenReturn(Mono.just(List.of(registration)));

        var registrations = controller.getOffenderRegistrations(CRN).block();

        assertThat(registrations).containsExactly(registration);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same probation status detail")
    @Test
    public void whenGetProbationStatusDetailFromCommunityApi_thenReturnIt() {
        featureFlags.setFlagValue("use-community-api-for-probation-status", true);

        var expectedDetail = ProbationStatusDetail.builder().build();
        when(offenderService.getProbationStatus(CRN)).thenReturn(Mono.just(expectedDetail));

        var probationStatusDetail = controller.getProbationStatusDetail(CRN).block();

        assertThat(probationStatusDetail).isSameAs(expectedDetail);
        verify(offenderService).getProbationStatus(CRN);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same custody")
    @Test
    public void whenGetCustody_thenReturnIt() {
        final var expectedCustody = Custody.builder().build();
        when(custodyService.getCustody(CRN, 12345L)).thenReturn(Mono.just(expectedCustody));

        var custody = controller.getCustody(CRN, 12345L).block();

        assertThat(custody).isSameAs(expectedCustody);
        verify(custodyService).getCustody(CRN, 12345L);
        verifyNoMoreInteractions(custodyService);
    }
}
