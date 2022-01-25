package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


class CourtCaseResponseMapperTest {

    private static final long ID = 1234L;
    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "CASE_NO";
    private static final String COURT_CODE = "COURT_CODE";
    private static final String COURT_ROOM = "COURT_ROOM";
    private static final boolean SUSPENDED_SENTENCE_ORDER = true;
    private static final boolean BREACH = true;
    private static final boolean PRE_SENTENCE_ACTIVITY = true;
    private static final LocalDateTime CREATED = LocalDateTime.now();
    private static final LocalDate HEARING_DATE = LocalDate.of(2020, 2, 25);
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(HEARING_DATE, LocalTime.of(9, 0));
    private static final LocalDate PREVIOUSLY_KNOWN_TERMINATION_DATE = LocalDate.of(2020, 2, 26);
    private static final String OFFENCE_TITLE = "OFFENCE_TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE_SUMMARY";
    private static final String ACT = "ACT";
    private static final String DEFENDANT_NAME = "DEFENDANT_NAME";
    private static final DefendantType DEFENDANT_TYPE = DefendantType.PERSON;
    private static final String DEFENDANT_ID = "81cdaec1-d197-4a70-b2a8-beeabdd05d21";
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String CRO = "CRO";
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 2, 26);
    private static final Sex DEFENDANT_SEX = Sex.NOT_KNOWN;
    private static final String NATIONALITY_1 = "NATIONALITY_1";
    private static final String NATIONALITY_2 = "NATIONALITY_2";
    private static final CourtSession SESSION = CourtSession.MORNING;
    private static final LocalDateTime FIRST_CREATED = LocalDateTime.of(2020, 1, 1, 1, 1);
    private CourtCaseEntity courtCaseEntity;
    private final AddressPropertiesEntity addressPropertiesEntity = AddressPropertiesEntity.builder()
        .line1("27")
        .line2("Elm Place")
        .line3("Bangor")
        .postcode("ad21 5dr")
        .build();
    private final NamePropertiesEntity namePropertiesEntity = NamePropertiesEntity.builder()
        .forename1("Wyatt")
        .forename2("Berry")
        .forename3("Stapp")
        .surname("Earp")
        .build();

    @BeforeEach
    void setUp() {
        List<DefendantEntity> defendants = Arrays.asList(
            DefendantEntity.builder().defendantName(DEFENDANT_NAME)
                .name(namePropertiesEntity)
                .sex(DEFENDANT_SEX)
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .dateOfBirth(DEFENDANT_DOB)
                .pnc(PNC)
                .defendantId(DEFENDANT_ID)
                .build()
        );

        var hearings = Arrays.asList(
            HearingEntity.builder()
                .hearingDay(HEARING_DATE)
                .hearingTime(SESSION_START_TIME.toLocalTime())
                .courtRoom(COURT_ROOM)
                .courtCode(COURT_CODE)
                .listNo(LIST_NO)
                .build(),
            HearingEntity.builder()
                .hearingDay(HEARING_DATE.plusDays(1))
                .hearingTime(SESSION_START_TIME.toLocalTime().plusHours(4))
                .courtRoom("02")
                .courtCode(COURT_CODE)
                .listNo("91st")
                .build()
        );

        courtCaseEntity = buildCourtCaseEntity(defendants, hearings, FIRST_CREATED);
    }

    @Test
    void givenSeparateDefendant_whenMap_thenReturnMultipleResponses() {
        // Build defendant with deliberately different values from the defaults
        var defendantOffence = DefendantOffenceEntity.builder()
            .act(ACT)
            .sequence(1)
            .summary(OFFENCE_SUMMARY)
            .title(OFFENCE_TITLE)
            .build();
        var defendantUuid = UUID.randomUUID().toString();
        var defendantName = NamePropertiesEntity.builder().title("DJ").forename1("Giles").surname("PETERSON").build();
        var defendantEntity = DefendantEntity.builder()
            .defendantName(defendantName.getFullName())
            .name(defendantName)
            .address(AddressPropertiesEntity.builder().postcode("WN8 0PZ").build())
            .sex(Sex.FEMALE)
            .nationality1("Romanian")
            .dateOfBirth(DEFENDANT_DOB.plusDays(2))
            .offender(OffenderEntity.builder()
                .crn("CRN123")
                .preSentenceActivity(true)
                .awaitingPsr(true)
                .suspendedSentenceOrder(true)
                .breach(true)
                .probationStatus(ProbationStatus.CURRENT)
                .previouslyKnownTerminationDate(LocalDate.now())
                .build())
            .pnc("PNC123")
            .cro("CRO123")
            .defendantId(defendantUuid)
            .type(DefendantType.PERSON)
            .nationality1("Romanian")
            .offences(singletonList(defendantOffence))
            .build();

        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(courtCaseEntity, defendantEntity, 3, HEARING_DATE);

        assertCaseFields(courtCaseResponse, null, SourceType.COMMON_PLATFORM);
        assertHearingFields(courtCaseResponse);
        assertThat(courtCaseResponse.getOffences()).hasSize(1);
        assertOffenceFields(courtCaseResponse.getOffences().get(0));

        assertThat(courtCaseResponse.getDefendantId()).isEqualTo(defendantUuid);
        assertThat(courtCaseResponse.getDefendantAddress().getPostcode()).isEqualTo("WN8 0PZ");
        assertThat(courtCaseResponse.getDefendantDob()).isEqualTo(DEFENDANT_DOB.plusDays(2));
        assertThat(courtCaseResponse.getDefendantSex()).isEqualTo("F");
        assertThat(courtCaseResponse.getDefendantName()).isEqualTo("DJ Giles PETERSON");
        assertThat(courtCaseResponse.getDefendantType()).isSameAs(DefendantType.PERSON);
        assertThat(courtCaseResponse.getName().getTitle()).isEqualTo("DJ");
        assertThat(courtCaseResponse.getName().getForename1()).isEqualTo("Giles");
        assertThat(courtCaseResponse.getName().getSurname()).isEqualTo("PETERSON");
        assertThat(courtCaseResponse.getCrn()).isEqualTo("CRN123");
        assertThat(courtCaseResponse.getPnc()).isEqualTo("PNC123");
        assertThat(courtCaseResponse.getCro()).isEqualTo("CRO123");
        assertThat(courtCaseResponse.getNationality1()).isEqualTo("Romanian");
        assertThat(courtCaseResponse.getNationality2()).isNull();
        assertThat(courtCaseResponse.getAwaitingPsr()).isTrue();
        assertThat(courtCaseResponse.getPreSentenceActivity()).isTrue();
        assertThat(courtCaseResponse.getBreach()).isTrue();
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isTrue();
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.now());
        assertThat(courtCaseResponse.getProbationStatus().toUpperCase()).isEqualTo("CURRENT");
        assertThat(courtCaseResponse.getProbationStatusActual()).isEqualTo("CURRENT");

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(3);
    }

    @Test
    void givenMultipleDefendants_whenMapByDefendantId_thenReturnCorrectDefendant() {

        var newName = NamePropertiesEntity.builder().surname("PRESLEY").forename1("Elvis").build();
        var defendant1 = EntityHelper.aDefendantEntity("bd1f71e5-939b-4580-8354-7d6061a58032")
            .withName(newName)
            .withOffender(OffenderEntity.builder().crn("D99999").build());
        var defendant2 = EntityHelper.aDefendantEntity(DEFENDANT_ID);

        var courtCase = courtCaseEntity.withDefendants(List.of(defendant1, defendant2));

        var response = CourtCaseResponseMapper.mapFrom(courtCase, "bd1f71e5-939b-4580-8354-7d6061a58032", 5);

        assertCaseFields(response, null);
        assertThat(response.getNumberOfPossibleMatches()).isEqualTo(5);
        assertThat(response.getCrn()).isEqualTo("D99999");
        assertThat(response.getName()).isEqualTo(newName);
    }

    @Test
    void givenDefendantWithOffender_whenMapByDefendantId_thenReturnFieldsFromOffender() {

        var newName = NamePropertiesEntity.builder().surname("TICKELL").forename1("Katherine").build();
        var defendant = EntityHelper.aDefendantEntity("bd1f71e5-939b-4580-8354-7d6061a58032")
            .withName(newName)
            .withOffender(OffenderEntity.builder().crn("W99999")
                .probationStatus(ProbationStatus.PREVIOUSLY_KNOWN)
                .previouslyKnownTerminationDate(LocalDate.now())
                .awaitingPsr(false)
                .breach(true)
                .preSentenceActivity(false)
                .suspendedSentenceOrder(true)
                .build());

        var courtCase = courtCaseEntity.withDefendants(List.of(defendant));

        var response = CourtCaseResponseMapper.mapFrom(courtCase, "bd1f71e5-939b-4580-8354-7d6061a58032", 5);

        assertThat(response.getProbationStatus()).isEqualTo("Previously known");
        assertThat(response.getCrn()).isEqualTo("W99999");
        assertThat(response.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.now());
        assertThat(response.getAwaitingPsr()).isFalse();
        assertThat(response.getBreach()).isTrue();
        assertThat(response.getPreSentenceActivity()).isFalse();
        assertThat(response.getSuspendedSentenceOrder()).isTrue();
    }

    private GroupedOffenderMatchesEntity buildMatchGroups() {
        return GroupedOffenderMatchesEntity.builder()
                        .offenderMatches(Arrays.asList(
                                OffenderMatchEntity.builder()
                                        .crn("1234")
                                        .build(),
                                OffenderMatchEntity.builder()
                                        .crn("2345")
                                        .build()
                        ))
                        .build();
    }

    private void assertCaseResponse(CourtCaseResponse courtCaseResponse, String caseNo, SourceType sourceType) {
        // Case based fields
        assertCaseFields(courtCaseResponse, caseNo, sourceType);

        // Hearing-based fields
        assertHearingFields(courtCaseResponse);

        // defendant-based fields
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUSLY_KNOWN_TERMINATION_DATE);
        assertThat(courtCaseResponse.getProbationStatus()).isSameAs(ProbationStatus.NOT_SENTENCED.getName());
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(courtCaseResponse.getBreach()).isEqualTo(BREACH);
        assertThat(courtCaseResponse.getPreSentenceActivity()).isEqualTo(PRE_SENTENCE_ACTIVITY);
        assertThat(courtCaseResponse.getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(courtCaseResponse.getDefendantType()).isEqualTo(DEFENDANT_TYPE);
        assertThat(courtCaseResponse.getDefendantId()).isEqualTo(DEFENDANT_ID);
        assertThat(courtCaseResponse.getDefendantAddress()).isEqualTo(addressPropertiesEntity);
        assertThat(courtCaseResponse.getName()).isEqualTo(namePropertiesEntity);
        assertThat(courtCaseResponse.getCrn()).isEqualTo(CRN);
        assertThat(courtCaseResponse.getPnc()).isEqualTo(PNC);
        assertThat(courtCaseResponse.getCro()).isEqualTo(CRO);
        assertThat(courtCaseResponse.getDefendantDob()).isEqualTo(DEFENDANT_DOB);
        assertThat(courtCaseResponse.getDefendantSex()).isEqualTo(DEFENDANT_SEX.getName());
        assertThat(courtCaseResponse.getNationality1()).isEqualTo(NATIONALITY_1);
        assertThat(courtCaseResponse.getNationality2()).isEqualTo(NATIONALITY_2);
        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(20);
        assertThat(courtCaseResponse.getAwaitingPsr()).isEqualTo(true);

        assertThat(courtCaseResponse.getOffences()).hasSize(2);
        assertOffenceFields(courtCaseResponse.getOffences().get(0));
    }

    private void assertOffenceFields(OffenceResponse offenceResponse) {
        assertThat(offenceResponse.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(offenceResponse.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);
        assertThat(offenceResponse.getAct()).isEqualTo(ACT);
        assertThat(offenceResponse.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);
    }

    private void assertHearingFields(CourtCaseResponse courtCaseResponse) {
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(courtCaseResponse.getListNo()).isEqualTo(LIST_NO);
        assertThat(courtCaseResponse.getSession()).isEqualTo(SESSION);
        assertThat(courtCaseResponse.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(courtCaseResponse.getHearings()).hasSize(2);
    }

    private void assertCaseFields(CourtCaseResponse courtCaseResponse, String caseNo, SourceType sourceType) {
        Optional.ofNullable(caseNo)
            .ifPresentOrElse((c) -> assertThat(courtCaseResponse.getCaseNo()).isEqualTo(c), () -> assertThat(courtCaseResponse.getCaseNo()).isNull());
        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getSource()).isEqualTo(sourceType.name());
        assertThat(courtCaseResponse.isCreatedToday()).isFalse();
    }

    private void assertCaseFields(CourtCaseResponse courtCaseResponse, String caseNo) {
        assertCaseFields(courtCaseResponse, caseNo, SourceType.COMMON_PLATFORM);
    }

    private CourtCaseEntity buildCourtCaseEntity(List<DefendantEntity> defendants, List<HearingEntity> hearings, LocalDateTime firstCreated) {

        return CourtCaseEntity.builder()
            .id(ID)
            .sourceType(SourceType.COMMON_PLATFORM)
            .caseNo(CASE_NO)
            .caseId(CASE_ID)
            .created(CREATED)
            .firstCreated(firstCreated)
            .defendants(defendants)
            .hearings(hearings)
            .build();
    }
}
