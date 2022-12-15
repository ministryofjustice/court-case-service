package uk.gov.justice.probation.courtcaseservice.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.URN;

@Provider("court-case-service")
@PactBroker(consumerVersionSelectors = @VersionSelector(consumer = "court-case-matcher", tag="${PACT_CONSUMER_TAG}", fallbackTag = "main"))
@PactFolder("src/test/resources/pact")
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


    @State({"a hearing exists for court B10JQ, case number 1600028913 and list number 2nd"})
    void getHearingByCaseNumber() {
        var hearingEntity = HearingEntity.builder()
                .hearingId("8bbb4fe3-a899-45c7-bdd4-4ee25ac5a83f")
                .hearingEventType(HearingEventType.RESULTED)
                .listNo("2nd")
                .courtCase(CourtCaseEntity.builder()
                        .caseNo("1600028913")
                        .sourceType(SourceType.LIBRA)
                        .urn(URN)
                        .build())
                .hearingDays(Collections.singletonList(HearingDayEntity.builder()
                        .courtCode("B10JQ")
                        .day(LocalDate.of(2021, 9, 11))
                        .time(LocalTime.of(9, 0))
                        .courtRoom("Courtroom 02")
                        .build()))
                .hearingDefendants(Collections.singletonList(HearingDefendantEntity.builder()
                        .defendant(DefendantEntity.builder()
                                .defendantId("51354F3C-9625-404D-B820-C74724D23484")
                                .personId("96624bb7-c64d-46d9-a427-813ec168f95a")
                                .crn("X340741")
                                .sex(Sex.MALE)
                                .offenderConfirmed(true)
                                .offender(OffenderEntity.builder()
                                        .crn("X340741")
                                        .preSentenceActivity(true)
                                        .previouslyKnownTerminationDate(LocalDate.of(2010, 1, 1))
                                        .probationStatus(OffenderProbationStatus.CURRENT)
                                        .suspendedSentenceOrder(true)
                                        .breach(true)
                                        .build())
                                .pnc("A/1234560BA")
                                .build())
                        .build()))
                .build();
        when(courtCaseService.getHearingByCaseNumber("B10JQ", "1600028913", "2nd")).thenReturn(hearingEntity);
    }

    @State({"a hearing exists for hearingId 8bbb4fe3-a899-45c7-bdd4-4ee25ac5a83f"})
    void getHearingByHearingId() {
        var hearingEntity = HearingEntity.builder()
                .hearingId("8bbb4fe3-a899-45c7-bdd4-4ee25ac5a83f")
                .courtCase(CourtCaseEntity.builder()
                        .caseId("D517D32D-3C80-41E8-846E-D274DC2B94A5")
                        .caseNo("D517D32D-3C80-41E8-846E-D274DC2B94A5")
                        .urn(URN)
                        .sourceType(SourceType.COMMON_PLATFORM)
                        .build())
                .hearingDefendants(List.of(HearingDefendantEntity.builder()
                                .defendant(DefendantEntity.builder()
                                        .address(AddressPropertiesEntity.builder()
                                                .line1("Address 1")
                                                .line2("Address 2")
                                                .line3("Address 3")
                                                .line4("Address 4")
                                                .line5("Address 5")
                                                .postcode("CF4 8BK")
                                                .build())
                                        .offenderConfirmed(true)
                                        .offender(OffenderEntity.builder()
                                                .crn("X346204")
                                                .preSentenceActivity(true)
                                                .previouslyKnownTerminationDate(LocalDate.of(2019, 5, 3))
                                                .probationStatus(OffenderProbationStatus.PREVIOUSLY_KNOWN)
                                                .awaitingPsr(true)
                                                .suspendedSentenceOrder(true)
                                                .breach(true)
                                                .build())
                                        .cro("ACRO")
                                        .dateOfBirth(LocalDate.of(1975, 4, 3))
                                        .defendantId("0ab7c3e5-eb4c-4e3f-b9e6-b9e78d3ea199")
                                        .personId("96624bb7-c64d-46d9-a427-813ec168f95a")
                                        .name(NamePropertiesEntity.builder()
                                                .forename1("Arthur")
                                                .forename2("Ponsonby")
                                                .forename3("Dude")
                                                .surname("Morgan")
                                                .title("Mr")
                                                .build())
                                        .pnc("pncpncpnc")
                                        .sex(Sex.MALE)
                                        .type(DefendantType.PERSON)
                                        .build())
                                .offences(List.of(OffenceEntity.builder()
                                        .act("Theft act")
                                        .summary("Did a theft")
                                        .title("Thievery")
                                        .judicialResults(List.of(JudicialResultEntity.builder()
                                                .isConvictedResult(false)
                                                .label("label")
                                                .judicialResultTypeId("judicialResultTypeId")
                                                .build()))
                                        .build()))
                                .build(),
                        HearingDefendantEntity.builder()
                                .defendant(DefendantEntity.builder()
                                        .address(AddressPropertiesEntity.builder()
                                                .line1("Address 1")
                                                .line2("Address 2")
                                                .line3("Address 3")
                                                .line4("Address 4")
                                                .line5("Address 5")
                                                .postcode("CF4 8BK")
                                                .build())

                                        .offender(OffenderEntity.builder()
                                                .crn("X346224")
                                                .preSentenceActivity(true)
                                                .previouslyKnownTerminationDate(LocalDate.of(2019, 5, 3))
                                                .probationStatus(OffenderProbationStatus.PREVIOUSLY_KNOWN)
                                                .awaitingPsr(true)
                                                .suspendedSentenceOrder(true)
                                                .breach(true)
                                                .pnc("offender_pnc")
                                                .build())
                                        .cro("ACRO")
                                        .dateOfBirth(LocalDate.of(1975, 4, 3))
                                        .defendantId("903c4c54-f667-4770-8fdf-1adbb5957c25")
                                        .personId("25429322-5e82-42dc-8005-858b5d082f80")
                                        .name(NamePropertiesEntity.builder()
                                                .forename1("John")
                                                .surname("Marston")
                                                .title("Mr")
                                                .build())
                                        .pnc("pncpncpnc")
                                        .sex(Sex.MALE)
                                        .type(DefendantType.PERSON)
                                        .build())
                                .offences(List.of(OffenceEntity.builder()
                                        .act("Theft act")
                                        .summary("Did a theft")
                                        .title("Thievery")
                                        .judicialResults(List.of(JudicialResultEntity.builder()
                                                .isConvictedResult(false)
                                                .label("label")
                                                .judicialResultTypeId("judicialResultTypeId")
                                                .build()))
                                        .build()))
                                .build()))

                .hearingDays(List.of(HearingDayEntity.builder()
                                .courtCode("B10JQ")
                                .courtRoom("4")
                                .time(LocalTime.of(10, 16, 51))
                                .day(LocalDate.of(2021, 8, 16))
                                .build(),
                        HearingDayEntity.builder()
                                .courtCode("B10JQ")
                                .courtRoom("3")
                                .time(LocalTime.of(10, 16, 51))
                                .day(LocalDate.of(2021, 8, 17))
                                .build()))
                .build();

        when(courtCaseService.getHearingByHearingId("8bbb4fe3-a899-45c7-bdd4-4ee25ac5a83f")).thenReturn(hearingEntity);
    }

    @State({"a hearing will be PUT by id"})
    void mockPutCourtCaseExtended() {
        final Mono<HearingEntity> caseMono = Mono.just(EntityHelper.aHearingEntity("X340741", "1600028914"));
        when(courtCaseService.createOrUpdateHearingByHearingId(eq("ABCDD32D-3C80-41E8-846E-D274DC2B94A5"), any(HearingEntity.class))).thenReturn(caseMono);
    }

    @State({"offender matches will be PUT"})
    void mockPutOffenderMatches() {
        when(offenderMatchService.createOrUpdateGroupedMatchesByDefendant(any(), any())).thenReturn(Mono.just(GroupedOffenderMatchesEntity.builder()
                .caseId("9b44418d-21a8-417d-a11a-dfe20164abaf")
                .defendantId("1df61bcb-7482-49b2-8f99-569458fb3203")
                        .offenderMatches(List.of(OffenderMatchEntity.builder()
                                        .matchProbability(0.12345d)
                                        .crn("X12340")
                                        .cro("12345")
                                        .pnc("2020/12345")
                                        .matchType(MatchType.NAME_DOB)
                                        .aliases(Collections.emptyList())
                                .build()))
                .build()));
    }
}
