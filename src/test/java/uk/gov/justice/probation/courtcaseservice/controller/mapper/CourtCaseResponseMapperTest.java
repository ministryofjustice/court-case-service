package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


class CourtCaseResponseMapperTest {

    private static final long ID = 1234L;
    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "CASE_NO";
    private static final String COURT_CODE = "COURT_CODE";
    private static final String COURT_ROOM = "COURT_ROOM";
    private static final String PROBATION_STATUS = "PROBATION_STATUS";
    private static final boolean SUSPENDED_SENTENCE_ORDER = true;
    private static final boolean BREACH = true;
    private static final boolean PRE_SENTENCE_ACTIVITY = true;
    private static final LocalDateTime CREATED = LocalDateTime.now();
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 25, 1, 0);
    private static final LocalDate PREVIOUSLY_KNOWN_TERMINATION_DATE = LocalDate.of(2020, 2, 26);
    private static final String OFFENCE_TITLE = "OFFENCE_TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE_SUMMARY";
    private static final String ACT = "ACT";
    private static final String DEFENDANT_NAME = "DEFENDANT_NAME";
    private static final DefendantType DEFENDANT_TYPE = DefendantType.PERSON;
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String CRO = "CRO";
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 2, 26);
    private static final String DEFENDANT_SEX = "DEFENDANT_SEX";
    private static final String NATIONALITY_1 = "NATIONALITY_1";
    private static final String NATIONALITY_2 = "NATIONALITY_2";
    private static final CourtSession SESSION = CourtSession.MORNING;
    private static final LocalDateTime FIRST_CREATED = LocalDateTime.of(2020, 1, 1, 1, 1);
    private CourtCaseEntity courtCaseEntity;
    private List<OffenceEntity> offences;
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
    private GroupedOffenderMatchesEntity matchGroups;

    @BeforeEach
    void setUp() {
        offences = Arrays.asList(
            OffenceEntity.builder().offenceTitle(OFFENCE_TITLE).offenceSummary(OFFENCE_SUMMARY).act(ACT).sequenceNumber(1).build(),
            OffenceEntity.builder().offenceTitle(OFFENCE_TITLE + "2").offenceSummary(OFFENCE_SUMMARY + "2").act(ACT + "2").sequenceNumber(2).build()
        );
        matchGroups = buildMatchGroups();
        courtCaseEntity = buildCourtCaseEntity(offences, FIRST_CREATED);
    }

    @Test
    void shouldMapEntityToResponse() {
        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(courtCaseEntity, matchGroups);

        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUSLY_KNOWN_TERMINATION_DATE);
        assertThat(courtCaseResponse.getProbationStatus()).isEqualTo(PROBATION_STATUS);
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(courtCaseResponse.getBreach()).isEqualTo(BREACH);
        assertThat(courtCaseResponse.getPreSentenceActivity()).isEqualTo(PRE_SENTENCE_ACTIVITY);
        assertThat(courtCaseResponse.getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(courtCaseResponse.getDefendantType()).isEqualTo(DEFENDANT_TYPE);
        assertThat(courtCaseResponse.getDefendantAddress()).isEqualTo(addressPropertiesEntity);
        assertThat(courtCaseResponse.getName()).isEqualTo(namePropertiesEntity);
        assertThat(courtCaseResponse.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(courtCaseResponse.getCrn()).isEqualTo(CRN);
        assertThat(courtCaseResponse.getSession()).isEqualTo(SESSION);
        assertThat(courtCaseResponse.getPnc()).isEqualTo(PNC);
        assertThat(courtCaseResponse.getCro()).isEqualTo(CRO);
        assertThat(courtCaseResponse.getListNo()).isEqualTo(LIST_NO);
        assertThat(courtCaseResponse.getDefendantDob()).isEqualTo(DEFENDANT_DOB);
        assertThat(courtCaseResponse.getDefendantSex()).isEqualTo(DEFENDANT_SEX);
        assertThat(courtCaseResponse.getNationality1()).isEqualTo(NATIONALITY_1);
        assertThat(courtCaseResponse.getNationality2()).isEqualTo(NATIONALITY_2);
        assertThat(courtCaseResponse.isCreatedToday()).isFalse();
        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(2);
    }

    @Test
    void shouldSetCreatedTodayToTrueIfCreatedToday() {
        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(buildCourtCaseEntity(offences, LocalDateTime.now()), matchGroups);
        assertThat(courtCaseResponse.isCreatedToday()).isTrue();
    }

    @Test
    void shouldReturn0IfNoPossibleMatches() {
        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(buildCourtCaseEntity(offences, FIRST_CREATED), null);

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(0);
    }

    @Test
    void shouldReturn0IfNullPossibleMatches() {
        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(courtCaseEntity, null);

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(0);
    }

    @Test
    void shouldMapOffencesToResponse() {
        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(courtCaseEntity, matchGroups);

        assertThat(courtCaseResponse.getOffences().size()).isEqualTo(2);

        var firstOffence = courtCaseResponse.getOffences().get(0);

        assertThat(firstOffence.getAct()).isEqualTo(ACT);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(firstOffence.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);


        var secondOffence = courtCaseResponse.getOffences().get(1);

        assertThat(secondOffence.getAct()).isEqualTo(ACT + "2");
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");
        assertThat(secondOffence.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY + "2");
    }

    @Test
    void shouldReflectOffenceSequenceNumberInResponseOrdering() {
        var reorderedOffences = offences.stream()
            .sorted(Comparator.comparing(OffenceEntity::getSequenceNumber))
            .collect(Collectors.toList());
        Collections.reverse(reorderedOffences);

        var reorderedCourtCaseEntity = buildCourtCaseEntity(reorderedOffences, FIRST_CREATED);

        var courtCaseResponse = CourtCaseResponseMapper.mapFrom(reorderedCourtCaseEntity, matchGroups);

        var firstOffence = courtCaseResponse.getOffences().get(0);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);

        var secondOffence = courtCaseResponse.getOffences().get(1);
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");

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

    private CourtCaseEntity buildCourtCaseEntity(List<OffenceEntity> offences, LocalDateTime firstCreated) {
        return CourtCaseEntity.builder()
            .id(ID)
            .pnc(PNC)
            .cro(CRO)
            .previouslyKnownTerminationDate(PREVIOUSLY_KNOWN_TERMINATION_DATE)
            .suspendedSentenceOrder(SUSPENDED_SENTENCE_ORDER)
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus(PROBATION_STATUS)
            .nationality2(NATIONALITY_2)
            .nationality1(NATIONALITY_1)
            .listNo(LIST_NO)
            .crn(CRN)
            .defendantSex(DEFENDANT_SEX)
            .defendantDob(DEFENDANT_DOB)
            .defendantName(DEFENDANT_NAME)
            .defendantAddress(addressPropertiesEntity)
            .name(namePropertiesEntity)
            .defendantType(DEFENDANT_TYPE)
            .courtRoom(COURT_ROOM)
            .courtCode(COURT_CODE)
            .caseNo(CASE_NO)
            .breach(BREACH)
            .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
            .caseId(CASE_ID)
            .created(CREATED)
            .offences(offences)
            .firstCreated(firstCreated)
            .build();
    }
}
