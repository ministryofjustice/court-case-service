package uk.gov.justice.probation.courtcaseservice.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;

import java.net.ConnectException;
import java.time.LocalDate;
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
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
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
    public static final String CONVICTION_ID = "123";

    @Mock
    private AssessmentsRestClient assessmentsRestClient;
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private DocumentRestClient documentRestClient;
    @Mock
    private List<Requirement> expectedRequirements;

    private final DocumentTypeFilter documentTypeFilter
        = new DocumentTypeFilter(singletonList(COURT_REPORT_DOCUMENT), singletonList("CJF"));

    private OffenderService service;
    private GroupedDocuments groupedDocuments;
    private Conviction conviction;
    private Assessment assessment;
    private Breach breach;

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
            .convictionId(CONVICTION_ID)
            .documents(Arrays.asList(courtReportDocumentDetail, cpsPackDocumentDetail))
            .build();
        this.groupedDocuments = GroupedDocuments.builder()
            .convictions(singletonList(documents))
            .documents(Collections.emptyList())
            .build();
        this.breach = Breach.builder()
            .id(2500020697L)
            .description("Community Order")
            .status("Breach Initiated")
            .started(LocalDate.of(2020,4,23))
            .build();
        this.conviction = Conviction.builder().convictionId(CONVICTION_ID).build();
        this.assessment = Assessment.builder().type("LAYER_3").completed(LocalDateTime.of(2020,4,23,10,5,20)).build();
        this.service = new OffenderService(offenderRestClient, assessmentsRestClient, documentRestClient, documentTypeFilter);
    }

    @DisplayName("Getting offender also includes calls to get convictions and conviction documents and merges the results")
    @Test
    void whenGetOffender_returnOffenderWithConvictionsDocumentsNotFiltered() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, false);

        assertThat(probationRecord).isNotNull();
        assertThat(probationRecord).isEqualTo(probationRecord);
        assertThat(probationRecord.getConvictions()).hasSize(1);
        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getBreaches()).hasSize(1);
        assertThat(conviction.getBreaches().get(0).getDescription()).isEqualTo("Community Order");
        assertThat(conviction.getBreaches().get(0).getStarted()).isEqualTo(LocalDate.of(2020,4,23));
        assertThat(conviction.getDocuments()).hasSize(2);
        assertThat(conviction.getDocuments().get(0).getDocumentName()).isEqualTo("PSR");
        assertThat(conviction.getDocuments().get(1).getDocumentName()).isEqualTo("CPS");
        assertThat(probationRecord.getAssessment().getType()).isEqualTo("LAYER_3");
        verify(offenderRestClient).getProbationRecordByCrn(CRN);
        verify(offenderRestClient).getConvictionsByCrn(CRN);
        verify(documentRestClient).getDocumentsByCrn(CRN);
        verify(assessmentsRestClient).getAssessmentByCrn(CRN);
        verifyNoMoreInteractions(offenderRestClient);
    }

    @DisplayName("Getting offender filtering out 1 of the 2 documents attached to the conviction")
    @Test
    public void whenGetOffender_returnOffenderWithConvictionsFilterDocuments() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, true);

        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getDocuments()).hasSize(1);
        assertThat(conviction.getDocuments().stream().filter(doc -> COURT_REPORT_DOCUMENT.equals(doc.getType())).findFirst().get().getDocumentName())
            .isEqualTo("PSR");
        verify(offenderRestClient).getProbationRecordByCrn(CRN);
        verify(offenderRestClient).getConvictionsByCrn(CRN);
        verify(documentRestClient).getDocumentsByCrn(CRN);
        verifyNoMoreInteractions(offenderRestClient);
    }

    @DisplayName("Getting offender throws exception when CRN not found, even if other calls succeed")
    @Test
    public void givenOffenderNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
    }

    @DisplayName("Getting offender convictions throws exception when CRN not found, even if other calls succeed")
    @Test
    public void givenConvictionsNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
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

    @DisplayName("Getting probation record does not throw exception when oasys assessment data is missing")
    @Test
    public void givenAssessmentNotFound_whenGetOffender_thenDoNotThrowException() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        // throw OffenderNotFoundException to simulate a 404 returned by assessments api
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, false);
        assertThat(probationRecord).isNotNull();

        // the assessment field should just be empty
        assertThat(probationRecord.getAssessment()).isNull();

        // but the rest of the record should be populated as normal
        assertThat(probationRecord.getConvictions()).hasSize(1);
        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getDocuments()).hasSize(2);
    }

    @DisplayName("Getting probation record does not throw exception when assessment api fails for any reason")
    @Test
    public void givenAssessmentRequestFails_whenGetOffender_thenDoNotThrowException() {
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        // throw ConnectException to simulate server side connection issues
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.error(new ConnectException("Connection refused")));

        ProbationRecord probationRecord = service.getProbationRecord(CRN, false);
        assertThat(probationRecord).isNotNull();

        // the assessment field should just be empty
        assertThat(probationRecord.getAssessment()).isNull();

        // but the rest of the record should be populated as normal
        assertThat(probationRecord.getConvictions()).hasSize(1);
        final Conviction conviction = probationRecord.getConvictions().get(0);
        assertThat(conviction.getDocuments()).hasSize(2);
    }

    @DisplayName("getting probation record throws exception if breach data is missing for a conviction")
    @Test
    public void givenBreachRequestFailsWith404_whenGetOffender_thenThrowException() {
        // all these are normal return values
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        // OffenderNotFound returned on 404 from community api
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

        assertThatExceptionOfType(OffenderNotFoundException.class)
            .isThrownBy(() -> service.getProbationRecord(CRN, true))
            .withMessageContaining(CRN);
    }

    @DisplayName("get probation record propagates connection errors from getBreaches()")
    @Test
    public void givenBreachRequestFailsWithConnectionIssue_whenGetOffender_thenThrowException() {
        // all these are normal return values
        when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(assessmentsRestClient.getAssessmentByCrn(CRN)).thenReturn(Mono.just(assessment));

        // throw ConnectException to simulate server side connection issues
        when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.error(new ConnectException("Connection refused")));

        // this actually throws `<reactor.core.Exceptions$ReactiveException: java.net.ConnectException: Connection refused>`
        // but i can't figure out how to test for that
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> service.getProbationRecord(CRN, true));
    }
}
