package uk.gov.justice.probation.courtcaseservice.pact;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
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
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import static org.mockito.ArgumentMatchers.any;
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


    @State({"a case exists for court B10JQ and case number 1600028913"})
    void getCourtCase() {
        var courtCaseEntity = CourtCaseEntity.builder()
            .courtCode("B10JQ")
            .caseNo("1600028913")
            .sessionStartTime(LocalDateTime.now())
            .defendantSex(Sex.MALE)
            .sourceType(SourceType.LIBRA)
            .defendants(Collections.singletonList(DefendantEntity.builder()
                    .defendantId("51354F3C-9625-404D-B820-C74724D23484")
                    .breach(true)
                    .crn("X340741")
                    .pnc("A/1234560BA")
                    .preSentenceActivity(true)
                    .previouslyKnownTerminationDate(LocalDate.of(2010, 01, 01))
                    .probationStatus(ProbationStatus.CURRENT.getName())
                    .suspendedSentenceOrder(true)
                    .build()))
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
                        .defendantId("0ab7c3e5-eb4c-4e3f-b9e6-b9e78d3ea199")
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
                        .sex(Sex.MALE)
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
                                .sex(Sex.MALE)
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
    void mockPutCourtCaseExtended() {
        final Mono<CourtCaseEntity> caseMono = Mono.just(EntityHelper.aCourtCaseEntity("X340741", "1600028914"));
        when(courtCaseService.createCase(eq("D517D32D-3C80-41E8-846E-D274DC2B94A5"), any(CourtCaseEntity.class))).thenReturn(caseMono);
    }
}
