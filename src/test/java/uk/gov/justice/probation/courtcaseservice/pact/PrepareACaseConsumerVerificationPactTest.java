package uk.gov.justice.probation.courtcaseservice.pact;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.Offence;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.Staff;
import uk.gov.justice.probation.courtcaseservice.service.model.Team;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ReportDocumentDates;

import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@Provider("court-case-service")
@PactBroker
@ActiveProfiles("unsecured")
class PrepareACaseConsumerVerificationPactTest extends BaseIntTest {


    public static final String CRN = "D991494";
    @MockBean
    private OffenderService offenderService;

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State({"a list of cases exist for the given date",
        "a case exists with the given case number",
        "the defendant has possible matches with existing offender records",
        "the defendant has an existing conviction",
        "will return the specific conviction breach details"})
    void stateFromIntegrationTests() {
    }

    @State({"an offender record exists"})
    void offenderRecordExists() {
        var offenderDetail = OffenderDetail.builder()
            .probationStatus(ProbationStatus.NO_RECORD)
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
            .previouslyKnownTerminationDate(LocalDate.of(2021, Month.FEBRUARY, 25))
            .build();

        when(offenderService.getProbationStatus(CRN)).thenReturn(Mono.just(probationStatusDetail));

        var assessment = Assessment.builder()
            .completed(LocalDateTime.of(2021, Month.MARCH, 21, 9,0, 0))
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
                .completedDate(LocalDateTime.of(2018, Month.FEBRUARY, 28, 0, 0,0)).build())
            .build();

        var breach1 = Breach.builder()
            .breachId(11131322L)
            .description("Community Order")
            .status("Breach Initiated")
            .started(LocalDate.of(2020, Month.OCTOBER, 20))
            .statusDate(LocalDate.of(2020, Month.DECEMBER, 18))
            .build();
        var breach2 = Breach.builder()
            .breachId(11131321L)
            .description("Community Order")
            .status("Breach Initiated")
            .started(LocalDate.of(2019, Month.OCTOBER, 20))
            .statusDate(LocalDate.of(2019, Month.DECEMBER, 18))
            .build();

        var conviction = Conviction.builder()
            .convictionId("2500295345")
            .active(true)
            .inBreach(true)
            .convictionDate(LocalDate.of(2019, Month.SEPTEMBER, 3))
            .offences(List.of(Offence.builder().description("Arson - 05600").build(), Offence.builder().description("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801").build()))
            .sentence(sentence)
            .endDate(LocalDate.of(2019, Month.JANUARY, 1))
            .documents(List.of(document))
            .breaches(List.of(breach1, breach2))
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
        when(offenderService.getOffenderRegistrations("D541487")).thenReturn(Mono.just(List.of(reg)));
    }

}
