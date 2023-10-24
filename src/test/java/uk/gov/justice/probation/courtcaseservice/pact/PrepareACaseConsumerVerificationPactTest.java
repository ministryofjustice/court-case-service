package uk.gov.justice.probation.courtcaseservice.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.Address;
import uk.gov.justice.probation.courtcaseservice.controller.model.Event;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper;
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.CustodyService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderUpdateService;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport.ReportAuthor;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.Offence;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.Staff;
import uk.gov.justice.probation.courtcaseservice.service.model.Team;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ReportDocumentDates;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.URN;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@Provider("court-case-service")
@PactBroker(consumerVersionSelectors = @VersionSelector(consumer = "prepare-a-case", tag="${PACT_CONSUMER_TAG}", fallbackTag = "main"))
@ActiveProfiles("unsecured")
class PrepareACaseConsumerVerificationPactTest extends BaseIntTest {

    public static final String CRN = "D991494";
    private static final String HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
    private static final String CASE_ID = "f76f1dfe-c41e-4242-b5fa-865d7dd2ce57";
    private static final String DEFENDANT_ID = "062c670d-fdf6-441f-99e1-d2ce0c3a3846";
    private static final String CASE_ID_2 = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
    private static final String DEFENDANT_ID_2 = "40db17d6-04db-11ec-b2d8-0242ac130002";
    @MockBean
    private OffenderService offenderService;
    @MockBean
    private OffenderMatchService offenderMatchService;
    @MockBean
    private CustodyService custodyService;
    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private CourtCaseService courtCaseService;
    @MockBean
    private OffenderUpdateService offenderUpdateService;
    @MockBean
    private CaseCommentsService caseCommentsService;
    @MockBean
    private AuthenticationHelper authenticationHelper;

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @Deprecated(forRemoval = true)
    @State({"a list of cases exist for the given date",
        "a case exists with the given case number",
        "the defendant has an existing conviction with sentence",
        "a defendant has an existing conviction",
        "will return the specific conviction breach details",
        "a defendant has an existing conviction"})
    @SuppressWarnings({"EmptyMethod"})
    void stateFromIntegrationTests() {
        // The data backing these states is created in the before-test.sql which was originally intended for application
        // local integration tests. Unfortunately by using this same data for Pact testing we've coupled this data with
        // the prepare-a-case Pact tests which means any changes to our integration test data can potentially break
        // the prepare-a-case contract. Instead of creating test data in before-test.sql we should instead use mocks at
        // the service layer to decouple our test data from prepare-a-case.
    }

    @State({"a case exists with the given case id and defendant id",
            "the defendant has possible matches with existing offender records",
            "an offender exists with the given defendantId",
            "a case exists with the given case id"
    })
    void existingCaseAndDefendant() {
        mockCase(CASE_ID, DEFENDANT_ID);
        mockCase(CASE_ID_2, DEFENDANT_ID_2);
    }

    private void mockCase(String caseId, String defendantId) {
        HearingEntity hearingEntity = buildCourtCaseEntity(caseId, PrepareACaseConsumerVerificationPactTest.HEARING_ID, defendantId);

        when(offenderUpdateService.updateDefendantOffender(eq(defendantId), any()))
                .thenReturn(Mono.just(hearingEntity.getHearingDefendant(defendantId).getDefendant().getOffender()));

        when(courtCaseService.getHearingByHearingIdAndDefendantId(eq(PrepareACaseConsumerVerificationPactTest.HEARING_ID), eq(defendantId)))
                .thenReturn(hearingEntity);

        when(offenderMatchService.getMatchCountByCaseIdAndDefendant(PrepareACaseConsumerVerificationPactTest.HEARING_ID, defendantId))
                .thenReturn(Optional.of(3));

        when(offenderMatchService.getOffenderMatchDetailsByDefendantId(defendantId))
                .thenReturn(Collections.singletonList(OffenderMatchDetail.builder()
                                .title("Mr")
                                .forename("Aadland")
                                .middleNames(List.of("Felix", "Hope"))
                                .surname("Bertrand")
                                .dateOfBirth(LocalDate.of(200, 7, 19))
                                .address(Address.builder()
                                        .addressNumber("19")
                                        .streetName("Junction Road")
                                        .district("Blackheath")
                                        .town("Sheffield")
                                        .county("South Yorkshire")
                                        .postcode("s10 2NA")
                                        .build())
                                .matchIdentifiers(MatchIdentifiers.builder()
                                        .crn("X320741")
                                        .pnc("2004/0712343H")
                                        .cro("123456/04A")
                                        .build())
                                .probationStatus(DefendantProbationStatus.PREVIOUSLY_KNOWN)
                                .event(Event.builder()
                                        .startDate(LocalDate.of(2014, 1, 1))
                                        .length(5)
                                        .lengthUnits("Years")
                                        .text("CJA - Indeterminate Public Prot.")
                                        .build())
                                .build()));

        String createdByUuid = "cc2285f2-91b0-4e00-bd5e-9bdc35896bb6";
        when(authenticationHelper.getAuthUserUuid(any())).thenReturn(createdByUuid);

        when(caseCommentsService.createCaseComment(any(CaseCommentEntity.class)))
            .thenReturn(CaseCommentEntity.builder()
                .createdBy("TEST.USER")
                .createdByUuid(createdByUuid)
                .caseId(CASE_ID)
                .id(12234L)
                .created(LocalDateTime.of(2022,8,24, 15,42, 41))  // 2022-08-24T15:42:41
                .author("Adam Sandler")
                .comment("A comment")
                .build());
    }

    private HearingEntity buildCourtCaseEntity(String caseId, String hearingId, String defendantId) {
        return HearingEntity.builder()
                .hearingId(hearingId)
                .listNo("3rd")
                .courtCase(CourtCaseEntity.builder()
                        .caseId(caseId)
                        .caseNo("1600028913")
                        .urn(URN)
                        .sourceType(SourceType.LIBRA)
                        .build())
                .hearingDays(Collections.singletonList(HearingDayEntity.builder()
                        .courtCode("B10JQ")
                        .courtRoom("1")
                        .time(LocalTime.of(9, 0))
                        .day(LocalDate.of(2019, 12, 4))
                        .build()))
                .hearingDefendants(Collections.singletonList(HearingDefendantEntity.builder()
                        .defendantId(defendantId)
                        .defendant(DefendantEntity.builder()
                                .defendantId(defendantId)
                                .pnc("A/1234560BA")
                                .defendantName("Mr Johnny BALL")
                                .name(NamePropertiesEntity.builder()
                                        .title("Mr")
                                        .forename1("Johnny")
                                        .forename2("John")
                                        .forename3("Jon")
                                        .surname("BALL")
                                        .build())
                                .address(AddressPropertiesEntity.builder()
                                        .line1("27")
                                        .line2("Elm Place")
                                        .line3("Bangor")
                                        .postcode("ad21 5dr")
                                        .build())
                                .dateOfBirth(LocalDate.of(1958, 10, 10))
                                .sex(Sex.MALE)
                                .type(DefendantType.PERSON)
                                .nationality1("British")
                                .nationality2("Polish")
                                .phoneNumber(PhoneNumberEntity.builder().home("07000000010").work("07000000011").mobile("07000000012").build())
                                .offender(OffenderEntity.builder()
                                        .crn("X320741")
                                        .pnc("PNC")
                                        .cro("CRO")
                                        .previouslyKnownTerminationDate(LocalDate.of(2010, 1, 1))
                                        .suspendedSentenceOrder(true)
                                        .breach(true)
                                        .preSentenceActivity(true)
                                        .awaitingPsr(true)
                                        .probationStatus(OffenderProbationStatus.CURRENT)
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    @State({"an offender record exists"})
    void offenderRecordExists() {
        var offenderDetail = OffenderDetail.builder()
                .probationStatus(DefendantProbationStatus.UNCONFIRMED_NO_RECORD)
                .forename("Floella")
                .middleNames(List.of("Karen", "Yunies"))
                .surname("Benjamin")
                .dateOfBirth(LocalDate.of(1949, Month.SEPTEMBER, 23))
                .title("Dame")
                .otherIds(OtherIds.builder()
                        .offenderId(198765L)
                        .crn(CRN)
                        .pncNumber("PNC/1122")
                        .croNumber("CRO/aass23")
                        .build())
                .build();
        when(offenderService.getOffenderDetail(CRN)).thenReturn(Mono.just(offenderDetail));

        var probationStatusDetail = ProbationStatusDetail.builder()
                .status("CURRENT")
                .preSentenceActivity(true)
                .inBreach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(LocalDate.of(2021, Month.FEBRUARY, 25))
                .build();

        when(offenderService.getProbationStatus(CRN)).thenReturn(Mono.just(probationStatusDetail));

        var assessment = Assessment.builder()
                .completed(LocalDateTime.of(2021, Month.MARCH, 21, 9, 0, 0))
                .status("COMPLETE")
                .type("type1")
                .build();

        var team = Team.builder()
                .description("OMIC OMU A")
                .telephone("0151 222 3333")
                .localDeliveryUnit("LDU Description")
                .district("OMiC POM Responsibility")
                .build();

        var offenderManager = OffenderManager.builder().active(true)
                .allocatedDate(LocalDate.of(2019, Month.SEPTEMBER, 30))
                .softDeleted(false)
                .provider("NPS North East")
                .team(team)
                .staff(Staff.builder()
                        .forenames("ANGEL")
                        .surname("Extravaganza")
                        .email("angel@heaven.com")
                        .telephone("02011112222")
                        .build())
                .build();

        var sentence = Sentence.builder()
                .sentenceId("123457")
                .description("CJA - Indeterminate Public Prot.")
                .length(5)
                .lengthInDays(1826)
                .lengthUnits("Years")
                .terminationDate(LocalDate.of(2019, Month.JANUARY, 1))
                .startDate(LocalDate.of(2014, Month.JANUARY, 1))
                .endDate(LocalDate.of(2019, Month.JANUARY, 1))
                .terminationReason("ICMS Miscellaneous Event")
                .build();

        var document = OffenderDocumentDetail.builder()
                .documentId("1d842fce-ec2d-45dc-ac9a-748d3076ca6b")
                .documentName("shortFormatPreSentenceReport_04092019_121424_OMIC_A_X320741.pdf")
                .author("Andy Marke")
                .type(DocumentType.COURT_REPORT_DOCUMENT)
                .extendedDescription("Pre-Sentence Report - Fast requested by Sheffield Crown Court on 04/09/2018")
                .createdAt(LocalDateTime.of(2019, Month.SEPTEMBER, 4, 0, 0, 0))
                .subType(KeyValue.builder()
                        .code("CJF")
                        .description("Pre-Sentence Report - Fast")
                        .build())
                .reportDocumentDates(ReportDocumentDates.builder()
                        .requestedDate(LocalDate.of(2018, Month.SEPTEMBER, 4))
                        .requiredDate(LocalDate.of(2019, Month.SEPTEMBER, 4))
                        .completedDate(LocalDateTime.of(2018, Month.FEBRUARY, 28, 0, 0, 0)).build())
                .build();

        var breachStartDate = LocalDate.of(2020, Month.OCTOBER, 20);
        var breachStatusDate = LocalDate.of(2020, Month.DECEMBER, 18);

        var rqmnt1 = getRequirement(2500083652L, LocalDate.of(2017, 6, 1), false, false);
        var rqmnt2 = getRequirement(2500007925L, LocalDate.of(2015, 7, 1), true, true);
        var licenceCondition1 = getLicenceCondition("Curfew Arrangement");
        var licenceCondition2 = getLicenceCondition("Participate or co-op with Programme or Activities");

        var conviction = Conviction.builder()
                .convictionId("2500295345")
                .active(true)
                .inBreach(true)
                .awaitingPsr(true)
                .convictionDate(LocalDate.of(2019, Month.SEPTEMBER, 3))
                .offences(List.of(Offence.builder()
                                .description("Arson - 05600")
                                .main(true)
                                .offenceDate(LocalDate.of(2017, 3, 8))
                                .build(),
                        Offence.builder()
                                .description("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801")
                                .offenceDate(LocalDate.of(2017, 3, 8))
                                .build()))
                .sentence(sentence)
                .endDate(LocalDate.of(2019, Month.JANUARY, 1))
                .documents(List.of(document))
                .breaches(List.of(getBreach(11131322L, breachStartDate, breachStatusDate), getBreach(11131321L, breachStartDate.minusYears(1), breachStatusDate.minusYears(1))))
                .requirements(List.of(rqmnt1, rqmnt2))
                .licenceConditions(List.of(licenceCondition1, licenceCondition2))
                .pssRequirements(Collections.emptyList())
                .psrReports(List.of(CourtReport.builder()
                        .courtReportId(1L)
                        .requestedDate(LocalDate.of(2019, 9, 3))
                        .requiredDate(LocalDate.of(2019, 9, 3))
                        .completedDate(LocalDate.of(2019, 9, 3))
                        .courtReportType(KeyValue.builder()
                                .description("Pre-Sentence Report - Fast")
                                .code("CJF")
                                .build())
                        .author(ReportAuthor.builder()
                                .unallocated(false)
                                .forenames("Reginald Kenneth")
                                .surname("Dwight")
                                .build())
                        .build()))
                .custodialType(KeyValue.builder().code("D").description("In Custody").build())
                .build();

        var probationRecord = ProbationRecord.builder()
                .crn(CRN)
                .assessment(assessment)
                .offenderManagers(List.of(offenderManager))
                .convictions(List.of(conviction))
                .build();

        when(offenderService.getProbationRecord(CRN, true)).thenReturn(probationRecord);
    }

    @State({"the defendant has an existing risk assessment"})
    void defendantHasAnExistingRiskAssessment() {
        var reg = Registration.builder()
                .active(false)
                .startDate(LocalDate.of(2019, Month.SEPTEMBER, 30))
                .endDate(LocalDate.of(2020, Month.JANUARY, 1))
                .type("Low RoSH")
                .nextReviewDate(LocalDate.of(2019, Month.DECEMBER, 30))
                .notes(List.of("01-01-20 - MAPPA has now ended."))
                .build();
        when(offenderService.getOffenderRegistrations("D991494")).thenReturn(Mono.just(List.of(reg)));
    }

    @State({"a defendant has an existing conviction"})
    void defendantHasAnExistingConviction() {
        var start = LocalDate.of(2017, Month.JUNE, 1);
        var end = LocalDate.of(2018, Month.MAY, 31);
        var breachStartDate = LocalDate.of(2020, Month.OCTOBER, 20);
        var breachStatusDate = LocalDate.of(2020, Month.DECEMBER, 18);

        var rqmnt1 = getRequirement(2500083652L, LocalDate.of(2017, 6, 1), false, false);
        var rqmnt2 = getRequirement(2500007925L, LocalDate.of(2015, 7, 1), true, true);
        var licenceCondition1 = getLicenceCondition("Curfew Arrangement");
        var licenceCondition2 = getLicenceCondition("Participate or co-op with Programme or Activities");

        var conviction = Conviction.builder()
                .convictionId("2500295343")
                .active(Boolean.TRUE)
                .inBreach(Boolean.FALSE)
                .convictionDate(start)
                .endDate(end)
                .custodialType(KeyValue.builder().code(CustodialStatus.RELEASED_ON_LICENCE.getCode()).description("Released - On Licence").build())
                .sentence(Sentence.builder()
                        .description("CJA - Community Order")
                        .length(12)
                        .lengthUnits("Months")
                        .lengthInDays(364)
                        .terminationDate(LocalDate.of(2017, Month.DECEMBER, 1))
                        .startDate(start)
                        .endDate(end)
                        .terminationReason("Completed - early good progress")
                        .build())
                .breaches(List.of(getBreach(11131322L, breachStartDate, breachStatusDate), getBreach(11131321L, breachStartDate.minusYears(1), breachStatusDate.minusYears(1))))
                .documents(List.of(OffenderDocumentDetail.builder()
                        .documentId("5058ca66-3751-4701-855a-86bf518d9392")
                        .documentName("documentName")
                        .author("Andy Marke")
                        .type(DocumentType.COURT_REPORT_DOCUMENT)
                        .createdAt(LocalDateTime.of(LocalDate.of(2019, Month.SEPTEMBER, 4), LocalTime.MIN))
                        .subType(KeyValue.builder()
                                .code("CR02")
                                .description("Court Report")
                                .build())
                        .build()))
                .requirements(List.of(rqmnt1, rqmnt2))
                .licenceConditions(List.of(licenceCondition1, licenceCondition2))
                .pssRequirements(Collections.emptyList())
                .offences(Collections.emptyList())
                .build();

        when(offenderService.getConviction("X320741", 2500295343L)).thenReturn(Mono.just(conviction));
    }

    @State({"a defendant has an existing conviction in custody"})
    void defendantHasAnExistingConvictionInCustody() {
        var custody = Custody.builder()
                .homeDetentionCurfewActualDate(LocalDate.of(2021, 8, 21))
                .homeDetentionCurfewEndDate(LocalDate.of(2021, 10, 21))
                .licenceExpiryDate(LocalDate.of(2022, 1, 21))
                .releaseDate(LocalDate.of(2021, 1, 21))
                .topupSupervisionStartDate(LocalDate.of(2021, 10, 21))
                .topupSupervisionExpiryDate(LocalDate.of(2022, 1, 21))
                .build();

        when(custodyService.getCustody("E654321", 345464567L)).thenReturn(Mono.just(custody));
    }

    private Breach getBreach(Long id, LocalDate startedDate, LocalDate statusDate) {
        return Breach.builder()
                .breachId(id)
                .description("Community Order")
                .status("Breach Initiated")
                .started(startedDate)
                .statusDate(statusDate)
                .build();
    }

    private LicenceCondition getLicenceCondition(String description) {
        return LicenceCondition.builder()
                .description(description)
                .startDate(LocalDate.of(2020, 2, 1))
                .subTypeDescription("ETE - High intensity")
                .notes("This is an example of licence condition notes")
                .build();
    }

    private Requirement getRequirement(Long id, LocalDate startDate, boolean active, boolean ad) {
        var builder = Requirement.builder()
                .requirementId(id)
                .startDate(startDate)
                .active(active);
        if (ad) {
            builder
                    .commencementDate(LocalDate.of(2015, 6, 29))
                    .adRequirementTypeMainCategory(KeyValue.builder().code("7").description("Court - Accredited Programme").build())
                    .adRequirementTypeSubCategory(KeyValue.builder().code("P12").description("ASRO").build());
        } else {
            builder
                    .terminationDate(LocalDate.of(2017, 12, 1))
                    .expectedStartDate(startDate)
                    .expectedEndDate(LocalDate.of(2017, 12, 1))
                    .requirementTypeSubCategory(KeyValue.builder().code("W01").description("Regular").build())
                    .requirementTypeMainCategory(KeyValue.builder().code("W").description("Unpaid Work").build())
                    .terminationReason(KeyValue.builder().code("74").description("Hours Completed Outside 12 months (UPW only)").build())
                    .length(60L)
                    .lengthUnit("Hours");
        }
        return builder.build();
    }
}
