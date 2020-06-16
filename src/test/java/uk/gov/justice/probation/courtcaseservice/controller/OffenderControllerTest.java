package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.service.BreachService;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;
import uk.gov.justice.probation.courtcaseservice.service.DocumentService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderControllerTest {
    private static final String CRN = "CRN";
    private static final String CONVICTION_ID = "CONVICTION_ID";
    private static final Long SENTENCE_ID = 5467L;
    static final Long SOME_EVENT_ID = 1234L;
    static final Long SOME_SENTENCE_ID = 1234L;

    @Mock
    private DocumentService documentService;
    @Mock
    private OffenderService offenderService;
    @Mock
    private ProbationRecord expectedProbationRecord;
    @Mock
    private Requirement expectedRequirement;
    @Mock
    private BreachResponse expectedBreach;
    @Mock
    private CurrentOrderHeaderResponse expectedCurrentOrderHeaderResponse;
    @Mock
    private ConvictionService convictionService;
    @Mock
    private BreachService breachService;

    private FeatureFlags featureFlags;

    private OffenderController controller;
    public static final long BREACH_CONVICTION_ID = 123455L;
    public static final long BREACH_ID = 1234456L;

    @BeforeEach
    void beforeEach() {
        featureFlags = new FeatureFlags();
        controller = new OffenderController(offenderService, convictionService, breachService, featureFlags, documentService);
    }

    @DisplayName("Normal service call returns response")
    @Test
    void callReturnsResponse() {
        final SentenceResponse attendancesResponse = SentenceResponse.builder().attendances(Collections.emptyList()).build();
        when(convictionService.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).thenReturn(attendancesResponse);

        assertThat(controller.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).isEqualTo(attendancesResponse);

        verify(convictionService).getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Feature toggle for sentence data is off")
    @Test
    void featureToggleFalseSentenceData() {
        featureFlags.setFlagValue("fetch-sentence-data", false);
        final SentenceResponse sentenceResponse = SentenceResponse.builder()
                .attendances(Collections.emptyList())
                .unpaidWork(UnpaidWork.builder().build())
                .build();
        when(convictionService.getConvictionOnly(CRN, SOME_EVENT_ID)).thenReturn(sentenceResponse);

        assertThat(controller.getSentence(CRN, SOME_EVENT_ID, SOME_SENTENCE_ID)).isEqualToComparingFieldByField(sentenceResponse);
        verify(convictionService).getConvictionOnly(CRN, SOME_EVENT_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same offender probation record")
    @Test
    public void whenGetProbationRecord_thenReturnIt() {

        final boolean applyFilter = true;
        when(offenderService.getProbationRecord(CRN, applyFilter)).thenReturn(expectedProbationRecord);

        ProbationRecord probationRecordResponse = controller.getProbationRecord(CRN, applyFilter);

        assertThat(probationRecordResponse).isNotNull();
        assertThat(probationRecordResponse).isEqualTo(expectedProbationRecord);
        verify(offenderService).getProbationRecord(CRN, applyFilter);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same list of requirements")
    @Test
    public void whenGetRequirements_thenReturnIt() {

        when(offenderService.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Collections.singletonList(expectedRequirement));

        RequirementsResponse requirementResponse = controller.getRequirements(CRN, CONVICTION_ID);

        assertThat(requirementResponse).isNotNull();
        assertThat(requirementResponse.getRequirements()).hasSize(1);
        assertThat(requirementResponse.getRequirements().get(0)).isEqualTo(expectedRequirement);
        verify(offenderService).getConvictionRequirements(CRN, CONVICTION_ID);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same current order header details")
    @Test
    public void whenGetCurrentOrderHeaderDetail_thenReturnIt() {

        when(convictionService.getCurrentOrderHeader(CRN, 1234L, SENTENCE_ID)).thenReturn(expectedCurrentOrderHeaderResponse);

        CurrentOrderHeaderResponse currentOrderHeaderResponse = controller.getCurrentOrderHeaderDetail(CRN, 1234L, SENTENCE_ID);

        assertThat(currentOrderHeaderResponse).isNotNull();
        assertThat(currentOrderHeaderResponse).isEqualTo(expectedCurrentOrderHeaderResponse);
        verify(convictionService).getCurrentOrderHeader(CRN, 1234L, SENTENCE_ID);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same breach")
    @Test
    public void whenGetBreach_thenReturnIt() {

        when(breachService.getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID)).thenReturn(expectedBreach);

        BreachResponse actualBreach = controller.getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID);

        assertThat(actualBreach).isNotNull();
        assertThat(actualBreach).isEqualTo(expectedBreach);
        verify(breachService).getBreach(CRN, BREACH_CONVICTION_ID, BREACH_ID);
    }

    @DisplayName("Ensures that the controller calls the document service and returns the same document")
    @Test
    public void whenGetDocument_thenReturnIt() {

        final ResponseEntity expectedResponse = mock(ResponseEntity.class);
        when(documentService.getDocument(CRN, CONVICTION_ID)).thenReturn(expectedResponse);

        HttpEntity responseEntity = controller.getOffenderDocumentByCrn(CRN, CONVICTION_ID);

        assertThat(responseEntity).isSameAs(expectedResponse);
        verify(documentService).getDocument(CRN, CONVICTION_ID);
    }

}
