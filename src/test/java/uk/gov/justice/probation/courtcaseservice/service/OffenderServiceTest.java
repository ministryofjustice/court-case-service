package uk.gov.justice.probation.courtcaseservice.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@ExtendWith(MockitoExtension.class)
class OffenderServiceTest {

    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";

    @Mock
    private AssessmentsRestClient assessmentsRestClient;
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private List<Requirement> expectedRequirements;

    private final DocumentTypeFilter documentTypeFilter
        = new DocumentTypeFilter(singletonList(COURT_REPORT_DOCUMENT), singletonList("CJF"));

    private OffenderService service;
    private GroupedDocuments groupedDocuments;
    private Conviction conviction;
    private Assessment assessment;

    @BeforeEach
    void beforeEach() {
        final OffenderDocumentDetail courtReportDocumentDetail = OffenderDocumentDetail.builder()
            .documentName("PSR")
            .type(COURT_REPORT_DOCUMENT)
            .subType(new KeyValue("CJF", "Pre-Sentence Report - Fast"))
            .build();
        final OffenderDocumentDetail cpsPackDocumentDetail = OffenderDocumentDetail.builder()
            .documentName("CPS")
            .type(DocumentType.CPSPACK_DOCUMENT)
            .build();
        final ConvictionDocuments documents = ConvictionDocuments.builder()
            .convictionId("123")
            .documents(Arrays.asList(courtReportDocumentDetail, cpsPackDocumentDetail))
            .build();
        this.groupedDocuments = GroupedDocuments.builder()
            .convictions(singletonList(documents))
            .documents(Collections.emptyList())
            .build();
        this.conviction = Conviction.builder().convictionId("123").build();
        this.assessment = Assessment.builder().type("LAYER_3").completed(LocalDateTime.of(2020,4,23,10,5,20)).build();
        this.service = new OffenderService(offenderRestClient, assessmentsRestClient, documentTypeFilter);
    }

    @DisplayName("Getting offender also includes calls to get convictions and conviction documents and merges the results")
    @Test
    void whenGetOffender_returnOffenderWithConvictionsDocumentsNotFiltered() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, false);

        assertThat(probationRecord).isNotNull();
        assertThat(probationRecord).isEqualTo(probationRecord);
        assertThat(probationRecord.getConvictions()).hasSize(1);
        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getDocuments()).hasSize(2);
        assertThat(conviction.getDocuments().get(0).getDocumentName()).isEqualTo("PSR");
        assertThat(conviction.getDocuments().get(1).getDocumentName()).isEqualTo("CPS");
        verify(offenderRestClient).getProbationRecordByCrn(CRN);
        verify(offenderRestClient).getConvictionsByCrn(CRN);
        verify(offenderRestClient).getDocumentsByCrn(CRN);
        verifyNoMoreInteractions(offenderRestClient);
    }

    @DisplayName("Getting offender filtering out 1 of the 2 documents attached to the conviction")
    @Test
    public void whenGetOffender_returnOffenderWithConvictionsFilterDocuments() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, true);

        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getDocuments()).hasSize(1);
        assertThat(conviction.getDocuments().stream().filter(doc -> COURT_REPORT_DOCUMENT.equals(doc.getType())).findFirst().get().getDocumentName())
            .isEqualTo("PSR");
        verify(offenderRestClient).getProbationRecordByCrn(CRN);
        verify(offenderRestClient).getConvictionsByCrn(CRN);
        verify(offenderRestClient).getDocumentsByCrn(CRN);
        verifyNoMoreInteractions(offenderRestClient);
    }

    @DisplayName("Getting offender throws exception when CRN not found, even if other calls succeed")
    @Test
    public void givenOffenderNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.empty());
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
    }

    @DisplayName("Getting offender convictions throws exception when CRN not found, even if other calls succeed")
    @Test
    public void givenConvictionsNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.empty());
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
    }

    @DisplayName("Getting offender convictions requirements")
    @Test
    public void whenGetConvictionRequirements_returnRequirements() {

        when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(expectedRequirements));

        List<Requirement> requirements = service.getConvictionRequirements(CRN, CONVICTION_ID);
        assertThat(requirements).isSameAs(expectedRequirements);
        verify(offenderRestClient).getConvictionRequirements(CRN, CONVICTION_ID);
    }
}
