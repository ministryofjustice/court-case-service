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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Provider("court-case-service")
@PactBroker(consumerVersionSelectors = @VersionSelector(consumer = "court-case-matcher"))
@ActiveProfiles("unsecured")
class CourtCaseMatcherVerificationPactTest extends BaseIntTest {

    @MockBean
    private OffenderMatchService offenderMatchService;

    @MockBean
    private CourtCaseService courtCaseService;

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State({"a case does not exist for court B10JQ and case number 1600028914"})
    void putCase() {
        var courtCaseEntity = EntityHelper.aCourtCaseEntity("X340741", "1600028914");
        when(courtCaseService.createCase(eq("B10JQ"), eq("1600028914"), any(CourtCaseEntity.class))).thenReturn(Mono.just(courtCaseEntity));
    }

    @State({"a case does not exist with grouped offender matches"})
    void postGroupedOffenderMatches() {
        when(offenderMatchService.createOrUpdateGroupedMatches(eq("B10JQ"), eq("1600028913"), any(GroupedOffenderMatchesRequest.class)))
            .thenReturn(Mono.just(GroupedOffenderMatchesEntity.builder().id(1234L).build()));
    }

    @State({"a case exists for court B10JQ and case number 1600028913"})
    void getCourtCase() {
        var courtCaseEntity = CourtCaseEntity.builder()
            .courtCode("B10JQ")
            .breach(true)
            .caseNo("1600028913")
            .crn("X340741")
            .pnc("A/1234560BA")
            .preSentenceActivity(true)
            .previouslyKnownTerminationDate(LocalDate.of(2010, Month.JANUARY, 1))
            .probationStatus("Current")
            .suspendedSentenceOrder(true)
            .sessionStartTime(LocalDateTime.now())
            .defendantSex("M")
            .sourceType(SourceType.LIBRA)
            .build();
        when(courtCaseService.getCaseByCaseNumber("B10JQ", "1600028913")).thenReturn(courtCaseEntity);
    }

    @State({"a case exists for caseId D517D32D-3C80-41E8-846E-D274DC2B94A5"})
    void getExtendedCourtCaseById() {
        var courtCaseEntity = CourtCaseEntity.builder()
                .courtCode("B10JQ")
                .caseId("D517D32D-3C80-41E8-846E-D274DC2B94A5")
                .caseNo("D517D32D-3C80-41E8-846E-D274DC2B94A5")
                .crn("X340741")
                .pnc("A/1234560BA")
                .sourceType(SourceType.LIBRA)
                .defendants(List.of(DefendantEntity.builder()
                        .address(AddressPropertiesEntity.builder()
                                .line1("Address 1")
                                .line2("Address 2")
                                .line3("Address 3")
                                .line4("Address 4")
                                .line5("Address 5")
                                .postcode("CF4 8BK")
                                .build())
                        .awaitingPsr(true)
                        .breach(true)
                        .crn("X346204")
                        .cro("ACRO")
                        .dateOfBirth(LocalDate.of(1975, 4, 3))
                        .defendantId("8E07B58D-3ED3-440E-9CC2-2BC94EDBC5AF")
                        .name(NamePropertiesEntity.builder()
                                .forename1("Arthur")
                                .forename2("Ponsonby")
                                .forename3("Dude")
                                .surname("Morgan")
                                .title("Mr")
                                .build())
                        .offences(List.of(DefendantOffenceEntity.builder()
                                .act("Theft act")
                                .summary("Did a theft")
                                .title("Thievery")
                                .build()))
                        .pnc("pncpncpnc")
                        .preSentenceActivity(true)
                        .previouslyKnownTerminationDate(LocalDate.of(2019, 5, 3))
                        .probationStatus("PREVIOUSLY_KNOWN")
                        .sex("M")
                        .suspendedSentenceOrder(true)
                        .type(DefendantType.PERSON)
                        .build(),
                        DefendantEntity.builder()
                                .address(AddressPropertiesEntity.builder()
                                        .line1("Address 1")
                                        .line2("Address 2")
                                        .line3("Address 3")
                                        .line4("Address 4")
                                        .line5("Address 5")
                                        .postcode("CF4 8BK")
                                        .build())
                                .awaitingPsr(true)
                                .breach(true)
                                .crn("X346224")
                                .cro("ACRO")
                                .dateOfBirth(LocalDate.of(1975, 4, 3))
                                .defendantId("903c4c54-f667-4770-8fdf-1adbb5957c25")
                                .name(NamePropertiesEntity.builder()
                                        .forename1("John")
                                        .surname("Marston")
                                        .title("Mr")
                                        .build())
                                .offences(List.of(DefendantOffenceEntity.builder()
                                        .act("Theft act")
                                        .summary("Did a theft")
                                        .title("Thievery")
                                        .build()))
                                .pnc("pncpncpnc")
                                .preSentenceActivity(true)
                                .previouslyKnownTerminationDate(LocalDate.of(2019, 5, 3))
                                .probationStatus("PREVIOUSLY_KNOWN")
                                .sex("M")
                                .suspendedSentenceOrder(true)
                                .type(DefendantType.PERSON)
                                .build()))

                .hearings(List.of(HearingEntity.builder()
                                .courtCode("B10JQ")
                                .courtRoom("4")
                                .hearingTime(LocalTime.of(10,16,51))
                                .hearingDay(LocalDate.of(2021,8, 16))
                                .listNo("4")
                                .build(),
                        HearingEntity.builder()
                                .courtCode("B10JQ")
                                .courtRoom("3")
                                .hearingTime(LocalTime.of(10,16,51))
                                .hearingDay(LocalDate.of(2021,8, 17))
                                .listNo("3")
                                .build()))
                .build();

        when(courtCaseService.getCaseByCaseId("D517D32D-3C80-41E8-846E-D274DC2B94A5")).thenReturn(courtCaseEntity);
    }

    @State({"a case will be PUT by id"})
    void getMinimalCourtCaseById() {
        final Mono<CourtCaseEntity> caseMono = Mono.just(EntityHelper.aCourtCaseEntity("X340741", "1600028914"));
        when(courtCaseService.createCase(anyString(), any(CourtCaseEntity.class))).thenReturn(caseMono);
    }
}
