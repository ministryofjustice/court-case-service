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
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport.ReportAuthor;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus.POST_SENTENCE_SUPERVISION;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.INSTITUTION_REPORT_DOCUMENT;

@DisplayName("OffenderService tests")
@ExtendWith(MockitoExtension.class)
class  OffenderServiceTest {

    private static final String CRN = "CRN";
    private static final Long CONVICTION_ID = 123L;
    private static final Long OFFENDER_ID = 12345L;
    private static final String PROBATION_STATUS = "CURRENT";
    private static final String PSS_DESC_TO_KEEP = "specified activity";

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

        private CommunityApiOffenderResponse communityApiOffenderResponse;
        private Requirement requirement;

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
                .convictionId(CONVICTION_ID.toString())
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
            this.requirement = getRequirement(LocalDate.of(2020, Month.APRIL, 23), true);
            this.conviction = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .build();
            this.assessment = Assessment.builder()
                .type("OLDER_COMPLETE_ASSESSMENT")
                .completed(LocalDateTime.of(2018,4,23,10,5,20))
                .status("COMPLETE")
                .build();
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            this.communityApiOffenderResponse = CommunityApiOffenderResponse.builder().build();
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
            service.setAssessmentStatuses(List.of("COMPLETE"));
            service.setPssRqmntDescriptionsKeepSubType(List.of(PSS_DESC_TO_KEEP));
            service.setPsrTypeCodes(List.of("CJF", "CJO", "CJS"));
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
            mockForStandardClientCalls(List.of(breach, breachMostRecent, breachNullStatusDate));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(this.requirement)));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord).isNotNull();
            assertThat(probationRecord.getConvictions()).hasSize(1);
            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getBreaches()).hasSize(3);
            assertThat(conviction.getBreaches().get(0).getDescription()).isEqualTo("Community Order");
            assertThat(conviction.getBreaches().get(0).getStarted()).isEqualTo(breachMostRecent.getStarted());
            assertThat(conviction.getBreaches().get(0).getStatusDate()).isEqualTo(breachMostRecent.getStatusDate());
            assertThat(conviction.getDocuments()).hasSize(2);
            assertThat(conviction.getDocuments().get(0).getDocumentName()).isEqualTo("PSR");
            assertThat(conviction.getDocuments().get(1).getDocumentName()).isEqualTo("CPS");
            assertThat(conviction.getRequirements()).hasSize(1);
            assertThat(conviction.getPssRequirements()).isEmpty();
            assertThat(conviction.getLicenceConditions()).isEmpty();
            assertThat(conviction.getPsrReports()).isEmpty();
            assertThat(probationRecord.getAssessment().getType()).isEqualTo("OLDER_COMPLETE_ASSESSMENT");
            verify(offenderRestClient).getOffenderManagers(CRN);
            verify(offenderRestClient).getConvictionsByCrn(CRN);
            verify(documentRestClient).getDocumentsByCrn(CRN);
            verify(assessmentsRestClient).getAssessmentsByCrn(CRN);
            verifyNoMoreInteractions(offenderRestClient, convictionRestClient);
        }

        @DisplayName("An offender on post sentence supervision has PSS requirements with no subtype description and but no licence conditions")
        @Test
        void givenPssCustodialStatus_whenGetProbationRecord_returnActivePssRequirementsStripSubTypeDescription() {

            var pssRqmnt1 = getPssRequirement("SPECIFIED ACTIVITY", "sub type desc", true);
            var pssRqmnt2 = getPssRequirement("specified activity", null, false);
            var pssRqmnt3 = getPssRequirement("description 3", "sub type description 3", true);
            var convictionPss = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .custodialType(KeyValue.builder().code(POST_SENTENCE_SUPERVISION.getCode()).build())
                .build();

            mockForStandardClientCalls(convictionPss);
            when(offenderRestClient.getConvictionRequirements(eq(CRN), any(Long.class))).thenReturn(Mono.just(List.of(requirement)));
            when(offenderRestClient.getConvictionPssRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(pssRqmnt1, pssRqmnt2, pssRqmnt3)));

            var probationRecord = service.getProbationRecord(CRN, false);

            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getRequirements()).hasSize(1);
            assertThat(conviction.getLicenceConditions()).isEmpty();
            assertThat(conviction.getPssRequirements()).hasSize(2);
            assertThat(conviction.getPssRequirements()).filteredOn(rqmnt -> rqmnt.getDescription().contains("SPECIFIED ACTIVITY")).hasSize(1);
            assertThat(conviction.getPssRequirements()).filteredOn(rqmnt -> rqmnt.getDescription().equals("description 3")).hasSize(1);
            assertThat(conviction.getPssRequirements()).extracting("subTypeDescription").containsOnly("sub type desc", null);
        }

        @DisplayName("An offender on licence has licence conditions and requirements but no PSS requirements")
        @Test
        void givenLicenceConditionCustodialStatus_whenGetProbationRecord_returnActiveLicenceConditions() {

            var licenceCondition1 = getLicenceCondition("description 1", true);
            var licenceCondition2 = getLicenceCondition("description 2", false);
            var convictionOnLicence = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .custodialType(KeyValue.builder().code(CustodialStatus.RELEASED_ON_LICENCE.getCode()).description("On Licence").build())
                .build();
            mockForStandardClientCalls(convictionOnLicence);
            when(offenderRestClient.getConvictionRequirements(eq(CRN), any(Long.class))).thenReturn(Mono.just(List.of(requirement)));
            when(offenderRestClient.getConvictionLicenceConditions(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(licenceCondition1, licenceCondition2)));

            var probationRecord = service.getProbationRecord(CRN, false);

            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getRequirements()).hasSize(1);
            assertThat(conviction.getLicenceConditions()).hasSize(1);
            assertThat(conviction.getLicenceConditions().get(0).getDescription()).isEqualTo("description 1");
            assertThat(conviction.getPssRequirements()).isEmpty();
        }

        @DisplayName("Getting offender filtering out 1 of the 2 documents attached to the conviction")
        @Test
        public void whenGetOffender_returnOffenderWithConvictionsFilterDocuments() {
            mockForStandardClientCalls(singletonList(conviction), singletonList(assessment));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(requirement)));

            var probationRecord = service.getProbationRecord(CRN, true);

            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.getDocuments()).hasSize(1);
            assertThat(conviction.getDocuments().stream().filter(doc -> COURT_REPORT_DOCUMENT.equals(doc.getType())).findFirst().get().getDocumentName())
                .isEqualTo("PSR");
            verify(offenderRestClient).getOffenderManagers(CRN);
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
            when(offenderRestClient.getConvictionRequirements(CRN, 123L)).thenReturn(Mono.just(emptyList()));

            var probationRecord = service.getProbationRecord(CRN, true);

            assertThat(probationRecord.getConvictions()).hasSize(3);
            assertThat(probationRecord.getConvictions()).containsExactly(conviction, conviction2, conviction3);
            verify(offenderRestClient).getOffenderManagers(CRN);
            verify(offenderRestClient).getConvictionsByCrn(CRN);
            verify(documentRestClient).getDocumentsByCrn(CRN);
            verifyNoMoreInteractions(offenderRestClient);
        }

        @DisplayName("Getting offender throws exception when CRN not found, even if other calls succeed")
        @Test
        void givenOffenderNotFound_whenGetOffender_thenThrowException() {
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.empty());

            assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
        }

        @DisplayName("Getting offender convictions throws exception when CRN not found, even if other calls succeed")
        @Test
        void givenConvictionsNotFound_whenGetOffender_thenThrowException() {
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.error(new OffenderNotFoundException(CRN)));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.empty());

            assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true))
                .withMessageContaining(CRN);
        }

        @DisplayName("Getting probation record does not throw exception when oasys assessment data is missing")
        @Test
        void givenAssessmentNotFound_whenGetOffender_thenDoNotThrowException() {
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
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
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(singletonList(breach)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));
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
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));

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
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord.getAssessment()).isNull();
        }

        @DisplayName("Handling of assessments when the service returns none")
        @Test
        void givenNoAssessments_whenGetOffender_thenFieldIsNull() {

            mockForStandardClientCalls(List.of(conviction), List.of());
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(emptyList()));

            var probationRecord = service.getProbationRecord(CRN, false);

            assertThat(probationRecord.getAssessment()).isNull();
        }

        @DisplayName("getting probation record throws exception if breach data is missing for a conviction")
        @Test
        void givenBreachRequestFailsWith404_whenGetOffender_thenThrowException() {
            // all these are normal return values
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.empty());
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(Collections.emptyList()));

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
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(singletonList(conviction)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.empty());

            // throw ConnectException to simulate server side connection issues
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.error(new ConnectException("Connection refused")));

            // this actually throws `<reactor.core.Exceptions$ReactiveException: java.net.ConnectException: Connection refused>`
            // but i can't figure out how to test for that
            assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> service.getProbationRecord(CRN, true));
        }

        @DisplayName("An offender on licence has licence conditions and requirements but no PSS requirements")
        @Test
        void givenaws() {

            var psrCourtReport = CourtReport.builder()
                .courtReportId(1L)
                .courtReportType(KeyValue.builder().code("CJF").description("Pre-Sentence Report - Fast").build())
                .author(ReportAuthor.builder().surname("Faulkner").forenames("Chris").build())
                .build();
            var nonPsrCourtReport = CourtReport.builder()
                .courtReportId(2L)
                .courtReportType(KeyValue.builder().code("XX").description("Some other report").build())
                .author(ReportAuthor.builder().surname("Faulkner").forenames("Chris").build())
                .build();
            var convictionPsrAwaits = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .awaitingPsr(true)
                .build();
            mockForStandardClientCalls(convictionPsrAwaits);
            when(convictionRestClient.getCourtReports(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(nonPsrCourtReport, psrCourtReport)));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(emptyList()));

            var probationRecord = service.getProbationRecord(CRN, false);

            var conviction = probationRecord.getConvictions().get(0);
            assertThat(conviction.isAwaitingPsr()).isTrue();
            assertThat(conviction.getPsrReports()).hasSize(1);
            verify(convictionRestClient).getCourtReports(CRN, CONVICTION_ID);
        }

        private void mockForStandardClientCalls(List<Breach> breaches) {
            mockForStandardClientCalls(singletonList(conviction), singletonList(assessment), breaches);
        }

        private void mockForStandardClientCalls(List<Conviction> convictions, List<Assessment> assessments) {
            mockForStandardClientCalls(convictions, assessments, singletonList(breach));
        }

        private void mockForStandardClientCalls(Conviction conviction) {
            mockForStandardClientCalls(singletonList(conviction), singletonList(assessment), singletonList(breach));
        }

        private void mockForStandardClientCalls(List<Conviction> convictions,
                                                List<Assessment> assessments,
                                                List<Breach> breaches) {
            when(offenderRestClient.getOffenderManagers(CRN)).thenReturn(Mono.just(Collections.emptyList()));
            when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(convictions));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(breaches));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(communityApiOffenderResponse));
            when(assessmentsRestClient.getAssessmentsByCrn(CRN)).thenReturn(Mono.just(assessments));
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for the method getProbationRecord")
    class ConvictionByIdTest {

        private GroupedDocuments groupedDocuments;
        private Conviction conviction;
        private Requirement requirement;

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
                                        .convictionId(CONVICTION_ID.toString())
                                        .documents(List.of(courtReportDocumentDetail))
                                        .build(),
                                    ConvictionDocuments.builder()
                                        .convictionId("83472343")
                                        .documents(List.of(nonCourtReport))
                                        .build()))
                .documents(Collections.emptyList())
                .build();
            this.conviction = Conviction.builder().convictionId(CONVICTION_ID.toString()).sentence(Sentence.builder().startDate(LocalDate.now()).build()).active(Boolean.TRUE).build();
            this.requirement = getRequirement(LocalDate.of(2018, Month.SEPTEMBER, 29), true);
            when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
            service = new OffenderService(offenderRestClientFactory, assessmentsRestClient, convictionRestClient, documentRestClient, documentTypeFilter, telemetryService);
            service.setPssRqmntDescriptionsKeepSubType(List.of(PSS_DESC_TO_KEEP));
        }

        @DisplayName("Getting conviction includes calls to get documents, breaches and requirements and merges the results")
        @Test
        void whenGetConviction_returnWithConvictionsDocumentsNotFiltered() {
            var inactiveRequirement = getRequirement(LocalDate.of(2018, Month.SEPTEMBER, 29), false);
            when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(conviction));
            var breach1 = getBreach(null);
            var breach2 = getBreach(LocalDate.of(2018, Month.SEPTEMBER, 29));
            var breach3 = getBreach(LocalDate.of(2019, Month.SEPTEMBER, 29));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(breach1, breach2, breach3)));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(requirement, inactiveRequirement)));

            var conviction = service.getConviction(CRN, CONVICTION_ID).block();

            assertThat(conviction.getActive()).isTrue();
            assertThat(conviction.getBreaches()).hasSize(3);
            assertThat(conviction.getBreaches().get(0).getStatusDate()).isEqualTo(LocalDate.of(2019, Month.SEPTEMBER, 29));
            assertThat(conviction.getBreaches().get(1).getStatusDate()).isEqualTo(LocalDate.of(2018, Month.SEPTEMBER, 29));
            assertThat(conviction.getBreaches().get(2).getStatusDate()).isNull();
            assertThat(conviction.getDocuments()).hasSize(1);
            assertThat(conviction.getRequirements()).hasSize(2);
            assertThat(conviction.getPssRequirements()).isEmpty();
            assertThat(conviction.getLicenceConditions()).isEmpty();
        }

        @DisplayName("Getting conviction in on licence status results in licence conditions in the conviction")
        @Test
        void givenOnLicenceStatus_whenGetConviction_thenReturnOnlyActiveLicenceConditions() {
            var convictionOnLicence = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .custodialType(KeyValue.builder().code(CustodialStatus.RELEASED_ON_LICENCE.getCode()).build())
                .build();
            var licenceCondition1 = getLicenceCondition("description", true);
            var licenceCondition2 = getLicenceCondition("description inactive", false);
            var emptyGroupedDocuments = GroupedDocuments.builder().documents(emptyList()).convictions(emptyList()).build();

            when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(convictionOnLicence));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(emptyList()));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(emptyGroupedDocuments));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(requirement)));
            when(offenderRestClient.getConvictionLicenceConditions(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(licenceCondition1, licenceCondition2)));

            var conviction = service.getConviction(CRN, CONVICTION_ID).block();

            assertThat(conviction.getActive()).isTrue();
            assertThat(conviction.getBreaches()).isEmpty();
            assertThat(conviction.getDocuments()).isEmpty();
            assertThat(conviction.getRequirements()).hasSize(1);
            assertThat(conviction.getPssRequirements()).isEmpty();
            assertThat(conviction.getLicenceConditions()).hasSize(1);
            assertThat(conviction.getLicenceConditions().get(0).getDescription()).isEqualTo("description");
        }

        @DisplayName("Getting conviction in PSS status results in PSS requirements in the conviction with filtering")
        @Test
        void givenPssStatus_whenGetConviction_thenReturn() {
            var convictionPss = Conviction.builder()
                .convictionId(CONVICTION_ID.toString())
                .sentence(Sentence.builder().startDate(LocalDate.now()).build())
                .active(Boolean.TRUE)
                .custodialType(KeyValue.builder().code(POST_SENTENCE_SUPERVISION.getCode()).build())
                .build();
            var pssRqmnt1 = getPssRequirement("SPECIFIED ACTIVITY", "sub type desc", true);
            var pssRqmnt2 = getPssRequirement("specified activity", null, false);
            var pssRqmnt3 = getPssRequirement("description 3", "sub type description 3", true);
            var emptyGroupedDocuments = GroupedDocuments.builder().documents(emptyList()).convictions(emptyList()).build();

            when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(convictionPss));
            when(offenderRestClient.getBreaches(CRN, CONVICTION_ID)).thenReturn(Mono.just(emptyList()));
            when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(emptyGroupedDocuments));
            when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(requirement)));
            when(offenderRestClient.getConvictionPssRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(pssRqmnt1, pssRqmnt2, pssRqmnt3)));

            var conviction = service.getConviction(CRN, CONVICTION_ID).block();

            assertThat(conviction.getActive()).isTrue();
            assertThat(conviction.getBreaches()).isEmpty();
            assertThat(conviction.getDocuments()).isEmpty();
            assertThat(conviction.getRequirements()).hasSize(1);
            assertThat(conviction.getLicenceConditions()).isEmpty();
            assertThat(conviction.getPssRequirements()).hasSize(2);
            assertThat(conviction.getPssRequirements()).extracting("subTypeDescription").containsOnly("sub type desc", null);
            assertThat(conviction.getPssRequirements()).filteredOn(rqmnt -> rqmnt.getDescription().contains("SPECIFIED ACTIVITY")).hasSize(1);
            assertThat(conviction.getPssRequirements()).filteredOn(rqmnt -> rqmnt.getDescription().equals("description 3")).hasSize(1);
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

    private Requirement getRequirement(LocalDate startDate, boolean active) {
        return Requirement.builder()
            .active(active)
            .startDate(startDate)
            .length(2L)
            .lengthUnit("months")
            .build();
    }

    private PssRequirement getPssRequirement(String description, String subTypeDescription, boolean active) {
        return PssRequirement.builder()
            .description(description)
            .subTypeDescription(subTypeDescription)
            .active(active)
            .build();
    }

    private LicenceCondition getLicenceCondition(String description, boolean active) {
        return LicenceCondition.builder()
            .description(description)
            .subTypeDescription("sub type desc")
            .active(active)
            .build();
    }

}
