package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.INSTITUTION_REPORT_DOCUMENT;

@DisplayName("OffenderService tests")
@ExtendWith(MockitoExtension.class)
class OffenderServiceTest {

    private static final String CRN = "CRN";
    private static final String CONVICTION_ID = "123";
    private static final Long OFFENDER_ID = 12345L;
    private static final String PROBATION_STATUS = "CURRENT";

    private final DocumentTypeFilter documentTypeFilter
        = new DocumentTypeFilter(singletonList(COURT_REPORT_DOCUMENT), singletonList("CJF"));

    @Mock
    private TelemetryService telemetryService;
    @Mock
    private AssessmentsRestClient assessmentsRestClient;
    @Mock
    private ConvictionRestClient convictionRestClient;
    @Mock
    private OffenderRestClientFactory offenderRestClientFactory;
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private DocumentRestClient documentRestClient;

    private OffenderService service;

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for the method getProbationRecord")
    class ProbationRecordTest {

        private GroupedDocuments groupedDocuments;
        private Conviction conviction;
        private Assessment assessment;
        private Breach breach;

        @BeforeEach
        void beforeEach() {
            var courtReportDocumentDetail = OffenderDocumentDetail.builder()
                .documentName("PSR")
                .type(COURT_REPORT_DOCUMENT)
                .subType(new KeyValue("CJF", "Pre-Sentence Report - Fast"))
                .build();
            var cpsPackDocumentDetail = OffenderDocumentDetail.builder()
                .documentName("CPS")
                .type(DocumentType.CPSPACK_DOCUMENT)
                .build();
            var documents = ConvictionDocuments.builder()
                .convictionId(CONVICTION_ID)
                .documents(Arrays.asList(courtReportDocumentDetail, cpsPackDocumentDetail))
                .build();
            this.groupedDocuments = GroupedDocuments.builder()
                .convictions(singletonList(documents))
                .documents(Collections.emptyList())
                .build();
            this.breach = Breach.builder()
                .breachId(2500020697L)
                .description("Community Order")
                .status("Breach Initiated")
                .started(LocalDate.of(2020,4,23))
                .statusDate(LocalDate.of(2020, Month.APRIL, 23))
                .build();
            var sentence = Sentence.builder().startDate(LocalDate.now()).build();
            this.conviction = Conviction.builder().convictionId(CONVICTION_ID).sentence(sentence).active(Boolean.TRUE).build();
            this.assessment = Assessment.builder()
                .type("OLDER_COMPLETE_ASSESSMENT")
                .completed(LocalDateTime.of(2018,4,23,10,5,20))
                .status("COMPLETE")
                .build();
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
            service.setAssessmentStatuses(List.of("COMPLETE"));
        }

        @DisplayName("Getting offender also includes calls to get convictions and conviction documents and merges the results")
        @Test
        void whenGetOffender_returnOffenderWithConvictionsDocumentsNotFiltered() {
            var breachMostRecent = Breach.builder()
                .breachId(101L)
                .description("Community Order")
                .status("Breach Initiated")
                .started(LocalDate.of(2021, Month.APRIL,23))
                .statusDate(LocalDate.of(2021, Month.JULY, 23))
                .build();
            var breachNullStatusDate = Breach.builder()
                .breachId(100L)
                .description("Community Order")
                .status("Breach Initiated")
                .started(LocalDate.of(2020, Month.APRIL,23))
                .build();
            mockForStandardClientCalls(singletonList(conviction), singletonList(assessment), List.of(breach, breachMostRecent, breachNullStatusDate));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord).isNotNull();
            assertThat(probationRecord).isEqualTo(probationRecord);
            assertThat(probationRecord.getConvictions()).hasSize(1);
            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getBreaches()).hasSize(3);
            assertThat(conviction.getBreaches().get(0).getDescription()).isEqualTo("Community Order");
            assertThat(conviction.getBreaches().get(0).getStarted()).isEqualTo(breachMostRecent.getStarted());
            assertThat(conviction.getBreaches().get(0).getStatusDate()).isEqualTo(breachMostRecent.getStatusDate());
            assertThat(conviction.getDocuments()).hasSize(2);
            assertThat(conviction.getDocuments().get(0).getDocumentName()).isEqualTo("PSR");
            assertThat(conviction.getDocuments().get(1).getDocumentName()).isEqualTo("CPS");
            assertThat(probationRecord.getAssessment().getType()).isEqualTo("OLDER_COMPLETE_ASSESSMENT");
            verify(offenderRestClient).getProbationRecordByCrn(CRN);
            verify(offenderRestClient).getConvictionsByCrn(CRN);
            verify(documentRestClient).getDocumentsByCrn(CRN);
            verify(assessmentsRestClient).getAssessmentsByCrn(CRN);
            verifyNoMoreInteractions(offenderRestClient);
        }

        @DisplayName("Getting offender filtering out 1 of the 2 documents attached to the conviction")
        @Test
        public void whenGetOffender_returnOffenderWithConvictionsFilterDocuments() {
            mockForStandardClientCalls(singletonList(conviction), singletonList(assessment));

            var probationRecord = service.getProbationRecord(CRN, true);

            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getDocuments()).hasSize(1);
            assertThat(conviction.getDocuments().stream().filter(doc -> COURT_REPORT_DOCUMENT.equals(doc.getType())).findFirst().get().getDocumentName())
                .isEqualTo("PSR");
            verify(offenderRestClient).getProbationRecordByCrn(CRN);
            verify(offenderRestClient).getConvictionsByCrn(CRN);
            verify(documentRestClient).getDocumentsByCrn(CRN);
            verifyNoMoreInteractions(offenderRestClient);
        }

        @DisplayName("Convictions being sorted in the response. Active then inactive.")
        @Test
        void whenGetOffender_returnOffenderWithConvictionsSorted() {

            var sentence2 = Sentence.builder().startDate(LocalDate.now().minusYears(3)).terminationDate(LocalDate.now().minusYears(1)).build();
            var conviction2 = Conviction.builder().convictionId("123").active(Boolean.TRUE).sentence(sentence2).build();
            var sentence3 = Sentence.builder().startDate(LocalDate.now().minusYears(1)).terminationDate(LocalDate.now().plusYears(1)).build();
            var conviction3 = Conviction.builder().convictionId("123").active(Boolean.FALSE).sentence(sentence3).build();

            mockForStandardClientCalls(List.of(conviction2, conviction3, conviction), singletonList(assessment));

            var probationRecord = service.getProbationRecord(CRN, true);

            assertThat(probationRecord.getConvictions()).hasSize(3);
            assertThat(probationRecord.getConvictions()).containsExactly(conviction, conviction2, conviction3);
            verify(offenderRestClient).getProbationRecordByCrn(CRN);
            verify(offenderRestClient).getConvictionsByCrn(CRN);
            verify(documentRestClient).getDocumentsByCrn(CRN);
            verifyNoMoreInteractions(offenderRestClient);
        }

        @DisplayName("Getting offender throws exception when CRN not found, even if other calls succeed")
        @Test
        void givenOffenderNotFound_whenGetOffender_thenThrowException() {
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(List.of(assessment)));

            assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
        }

        @DisplayName("Getting offender convictions throws exception when CRN not found, even if other calls succeed")
        @Test
        void givenConvictionsNotFound_whenGetOffender_thenThrowException() {
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(List.of(assessment)));

            assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
        }

        @DisplayName("Getting probation record does not throw exception when oasys assessment data is missing")
        @Test
        void givenAssessmentNotFound_whenGetOffender_thenDoNotThrowException() {
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            // throw OffenderNotFoundException to simulate a 404 returned by assessments api
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

            var probationRecord = service.getProbationRecord(CRN, false);
            assertThat(probationRecord).isNotNull();

            // the assessment field should just be empty
            assertThat(probationRecord.getAssessment()).isNull();

            // but the rest of the record should be populated as normal
            assertThat(probationRecord.getConvictions()).hasSize(1);
            final Conviction conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getDocuments()).hasSize(2);

            verify(telemetryService)
                .trackApplicationDegradationEvent(eq("assessment data missing from probation record (CRN '" + CRN + "' not found in oasys)"),
                                                any(OffenderNotFoundException.class),
                                                eq(CRN));
        }

        @DisplayName("Getting probation record does not throw exception when assessment api fails for any reason")
        @Test
        void givenAssessmentRequestFails_whenGetOffender_thenDoNotThrowException() {
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            // throw ConnectException to simulate server side connection issues
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.error(new ConnectException("Connection refused")));

            var probationRecord = service.getProbationRecord(CRN, false);
            assertThat(probationRecord).isNotNull();

            // the assessment field should just be empty
            assertThat(probationRecord.getAssessment()).isNull();

            // but the rest of the record should be populated as normal
            assertThat(probationRecord.getConvictions()).hasSize(1);
            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getDocuments()).hasSize(2);

            verify(telemetryService)
                .trackApplicationDegradationEvent(eq("call failed to get assessment data for for CRN '" + CRN + "'"),
                    any(Exception.class),
                    eq(CRN));
        }

        @DisplayName("Get the most recent COMPLETE assessment, ignore the more recent PENDING one")
        @Test
        void givenAssessmentsRequests_whenGetOffender_thenFilterForMostRecentComplete() {

            var assessmentPending = Assessment.builder()
                .status("PENDING")
                .completed(LocalDateTime.now())
                .build();
            var assessmentComplete = Assessment.builder()
                .status("COMPLETE")
                .type("NEWEST_COMPLETE_ASSESSMENT")
                .completed(LocalDateTime.now().minusMinutes(1))
                .build();

            mockForStandardClientCalls(List.of(conviction), List.of(assessmentPending, assessment, assessmentComplete));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord.getAssessment()).isSameAs(assessmentComplete);
        }

        @DisplayName("No assessment if the list has only non-COMPLETE ones")
        @Test
        void givenNoCompleteAssessments_whenGetOffender_thenFieldIsNull() {

            var assessmentPending = Assessment.builder()
                .status("PENDING")
                .completed(LocalDateTime.now())
                .build();

            mockForStandardClientCalls(List.of(conviction), List.of(assessmentPending));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord.getAssessment()).isNull();
        }

        @DisplayName("Handling of assessments when the service returns none")
        @Test
        void givenNoAssessments_whenGetOffender_thenFieldIsNull() {

            mockForStandardClientCalls(List.of(conviction), List.of());

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord.getAssessment()).isNull();
        }

        @DisplayName("getting probation record throws exception if breach data is missing for a conviction")
        @Test
        void givenBreachRequestFailsWith404_whenGetOffender_thenThrowException() {
            // all these are normal return values
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(List.of(assessment)));

            // OffenderNotFound returned on 404 from community api
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));

            assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
        }

        @DisplayName("get probation record propagates connection errors from getBreaches()")
        @Test
        void givenBreachRequestFailsWithConnectionIssue_whenGetOffender_thenThrowException() {
            // all these are normal return values
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(List.of(assessment)));

            // throw ConnectException to simulate server side connection issues
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.error(new ConnectException("Connection refused")));

            // this actually throws `<reactor.core.Exceptions$ReactiveException: java.net.ConnectException: Connection refused>`
            // but i can't figure out how to test for that
            assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true));
        }

        private void mockForStandardClientCalls(List<Conviction> convictions, List<Assessment> assessments) {
            mockForStandardClientCalls(convictions, assessments, singletonList(breach));
        }

        private void mockForStandardClientCalls(List<Conviction> convictions, List<Assessment> assessments, List<Breach> breaches) {
            when(offenderRestClient.getProbationRecordByCrn(CRN)).thenReturn(Mono.just(ProbationRecord.builder().crn(CRN).build()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(convictions));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(breaches));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(assessments));
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for the method getProbationRecord")
    class ConvictionByIdTest {

        private GroupedDocuments groupedDocuments;
        private Conviction conviction;

        @BeforeEach
        void beforeEach() {

            var courtReportDocumentDetail = OffenderDocumentDetail.builder()
                .documentName("PSR")
                .type(COURT_REPORT_DOCUMENT)
                .subType(new KeyValue("CJF", "Pre-Sentence Report - Fast"))
                .build();
            var nonCourtReport = OffenderDocumentDetail.builder()
                .documentName("PSR")
                .type(INSTITUTION_REPORT_DOCUMENT)
                .subType(new KeyValue("CJF", "Pre-Sentence Report - Fast"))
                .build();
            this.groupedDocuments = GroupedDocuments.builder()
                .convictions(List.of(ConvictionDocuments.builder()
                                        .convictionId(CONVICTION_ID)
                                        .documents(List.of(courtReportDocumentDetail))
                                        .build(),
                                    ConvictionDocuments.builder()
                                        .convictionId("83472343")
                                        .documents(List.of(nonCourtReport))
                                        .build()))
                .documents(Collections.emptyList())
                .build();
            var sentence = Sentence.builder().startDate(LocalDate.now()).build();
            this.conviction = Conviction.builder().convictionId(CONVICTION_ID).sentence(sentence).active(Boolean.TRUE).build();
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
        }

        @DisplayName("Getting offender also includes calls to get convictions and conviction documents and merges the results")
        @Test
        void whenGetOffender_returnOffenderWithConvictionsDocumentsNotFiltered() {
            var convictionId = Long.parseLong(CONVICTION_ID);
            when(convictionRestClient.getConviction(CRN, convictionId)).thenReturn(Mono.just(conviction));
            var breach1 = getBreach(null);
            var breach2 = getBreach(LocalDate.of(2018, Month.SEPTEMBER, 29));
            var breach3 = getBreach(LocalDate.of(2019, Month.SEPTEMBER, 29));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(breach1, breach2, breach3)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));

            var conviction = service.getConviction(CRN, convictionId).block();

            assertThat(conviction.getActive()).isTrue();
            assertThat(conviction.getBreaches()).hasSize(3);
            assertThat(conviction.getBreaches().get(0).getStatusDate()).isEqualTo(LocalDate.of(2019, Month.SEPTEMBER, 29));
            assertThat(conviction.getBreaches().get(1).getStatusDate()).isEqualTo(LocalDate.of(2018, Month.SEPTEMBER, 29));
            assertThat(conviction.getBreaches().get(2).getStatusDate()).isNull();
            assertThat(conviction.getDocuments()).hasSize(1);
        }

    }

    @Nested
    @DisplayName("Tests for the method getOffenderDetail")
    class OffenderDetailTest {

        @BeforeEach
        void beforeEach() {
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
        }

        @DisplayName("Simple get of offender detail")
        @Test
        void whenGetOffenderDetail_thenReturnDetailWithCorrectProbationStatus() {
            final var communityApiOffenderResponse = CommunityApiOffenderResponse.builder()
                    .offenderId(OFFENDER_ID)
                    .otherIds(OtherIds.builder()
                            .crn("CRN")
                            .build())
                    .build();
            final var probationStatusDetail = ProbationStatusDetail.builder()
                    .status(PROBATION_STATUS)
                    .build();

            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(offenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));

            var detail = service.getOffenderDetail(CRN).block();

            assertThat(detail.getOtherIds().getOffenderId()).isEqualTo(OFFENDER_ID);
            assertThat(detail.getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
        }
    }

    @Nested
    @DisplayName("Tests for the method getOffenderRegistrations")
    class OffenderRegistrationsTest {

        @BeforeEach
        void beforeEach() {
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
        }

        @DisplayName("Simple get of offender registrations")
        @Test
        void whenGetOffenderRegistrations_thenReturnSame() {
            var registration = Registration.builder().build();
            when(offenderRestClient.getOffenderRegistrations(CRN)).thenReturn(Mono.just(List.of(registration)));

            var registrations = service.getOffenderRegistrations(CRN).block();

            assertThat(registrations).containsExactly(registration);
        }
    }

    @Nested
    @DisplayName("Tests for the method getProbationStatusDetail")
    class ProbationStatusDetailTest {

        @BeforeEach
        void beforeEach() {
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
        }

        @Test
        void whenGetProbationStatus_thenReturn() {
            var probationStatus = ProbationStatusDetail.builder().status(ProbationStatus.CURRENT.name()).build();
            when(offenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatus));

            var probationStatusDetail = service.getProbationStatus(CRN).blockOptional();

            assertThat(probationStatusDetail).hasValue(probationStatus);
        }
    }

    private Breach getBreach(LocalDate statusDate) {
        return Breach.builder()
            .breachId(2500020697L)
            .description("Community Order")
            .status("Breach Initiated")
            .started(LocalDate.of(2020, 4, 23))
            .statusDate(statusDate)
            .build();
    }
}
