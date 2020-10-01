package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.ImmutableOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class CourtCaseResponseMapperTest {

    private static final long ID = 1234L;
    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "CASE_NO";
    private static final String COURT_CODE = "COURT_CODE";
    private static final String COURT_ROOM = "COURT_ROOM";
    private static final String PROBATION_STATUS = "PROBATION_STATUS";
    private static final boolean SUSPENDED_SENTENCE_ORDER = true;
    private static final boolean BREACH = true;
    private static final LocalDateTime LAST_UPDATED = LocalDateTime.of(2020, 2, 24, 1, 0);
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 25, 1, 0);
    private static final LocalDate PREVIOUSLY_KNOWN_TERMINATION_DATE = LocalDate.of(2020, 2, 26);
    private static final String OFFENCE_TITLE = "OFFENCE_TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE_SUMMARY";
    private static final String ACT = "ACT";
    private static final String DEFENDANT_NAME = "DEFENDANT_NAME";
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String CRO = "CRO";
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 2, 26);
    private static final String DEFENDANT_SEX = "DEFENDANT_SEX";
    private static final String NATIONALITY_1 = "NATIONALITY_1";
    private static final String NATIONALITY_2 = "NATIONALITY_2";
    private static final CourtSession SESSION = CourtSession.MORNING;
    private CourtCaseEntity courtCaseEntity;
    private List<ImmutableOffenceEntity> offences;
    private final CourtCaseResponseMapper courtCaseResponseMapper = new CourtCaseResponseMapper();
    private final AddressPropertiesEntity addressPropertiesEntity = new AddressPropertiesEntity("27", "Elm Place", "ad21 5dr", "Bangor", null, null);

    @Before
    public void setUp() {
        offences = Arrays.asList(
            ImmutableOffenceEntity.builder().offenceTitle(OFFENCE_TITLE).offenceSummary(OFFENCE_SUMMARY).act(ACT).sequenceNumber(1).build(),
            ImmutableOffenceEntity.builder().offenceTitle(OFFENCE_TITLE + "2").offenceSummary(OFFENCE_SUMMARY + "2").act(ACT + "2").sequenceNumber(2).build()
        );
        courtCaseEntity = buildCourtCaseEntity(offences, buildMatchGroups());
    }

    @Test
    public void shouldMapEntityToResponse() {
        var courtCaseResponse = courtCaseResponseMapper.mapFrom(courtCaseEntity);

        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo(COURT_ROOM);
        // TODO: Delete this field
//        assertThat(courtCaseResponse.getLastUpdated()).isEqualTo(LAST_UPDATED);
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUSLY_KNOWN_TERMINATION_DATE);
        assertThat(courtCaseResponse.getProbationStatus()).isEqualTo(PROBATION_STATUS);
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(courtCaseResponse.getBreach()).isEqualTo(BREACH);
        assertThat(courtCaseResponse.getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(courtCaseResponse.getDefendantAddress()).isEqualTo(addressPropertiesEntity);
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
        assertThat(courtCaseResponse.isRemoved()).isFalse();
        assertThat(courtCaseResponse.isCreatedToday()).isTrue();
        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(3);
    }

    @Test
    public void shouldReturn0IfNoPossibleMatches() {
        var courtCaseResponse = courtCaseResponseMapper.mapFrom(
                courtCaseEntity = buildCourtCaseEntity(offences, Collections.emptyList()));

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(0);
    }


    @Test
    public void shouldReturn0IfNullPossibleMatches() {
        var courtCaseResponse = courtCaseResponseMapper.mapFrom(
                courtCaseEntity = buildCourtCaseEntity(offences, null));

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(0);
    }

    @Test
    public void shouldMapOffencesToResponse() {
        var courtCaseResponse = courtCaseResponseMapper.mapFrom(courtCaseEntity);

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
    public void shouldReflectOffenceSequenceNumberInResponseOrdering() {
        var reorderedOffences = offences.stream()
            .sorted(Comparator.comparing(ImmutableOffenceEntity::getSequenceNumber))
            .collect(Collectors.toList());
        Collections.reverse(reorderedOffences);

        var reorderedCourtCaseEntity = buildCourtCaseEntity(reorderedOffences, buildMatchGroups());

        var courtCaseResponse = courtCaseResponseMapper.mapFrom(reorderedCourtCaseEntity);

        var firstOffence = courtCaseResponse.getOffences().get(0);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);

        var secondOffence = courtCaseResponse.getOffences().get(1);
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");

    }

    public List<GroupedOffenderMatchesEntity> buildMatchGroups() {
        return Arrays.asList(
                GroupedOffenderMatchesEntity.builder()
                        .offenderMatches(Arrays.asList(
                                OffenderMatchEntity.builder()
                                        .crn("1234")
                                        .build(),
                                OffenderMatchEntity.builder()
                                        .crn("2345")
                                        .build()
                        ))
                        .build(),
                GroupedOffenderMatchesEntity.builder()
                        .offenderMatches(Arrays.asList(
                                OffenderMatchEntity.builder()
                                        .crn("1234")
                                        .build(),
                                OffenderMatchEntity.builder()
                                        .crn("3456")
                                        .build()
                        ))
                        .build()
        );
    }

    private CourtCaseEntity buildCourtCaseEntity(List<ImmutableOffenceEntity> offences, List<GroupedOffenderMatchesEntity> matchGroups) {
        CourtCaseEntity courtCase = CourtCaseEntity.builder()
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
            .courtRoom(COURT_ROOM)
            .courtCode(COURT_CODE)
            .caseNo(CASE_NO)
            .breach(BREACH)
            .caseId(CASE_ID)
            .created(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))
            .deleted(false)
            .lastUpdated(LAST_UPDATED)
            .offences(offences)
            .groupedOffenderMatches(matchGroups)
            .build();
        courtCase.setLastUpdated(LAST_UPDATED);
        List<ImmutableOffenceEntity> updatedOffences = offences.stream()
                .map(offenceEntity -> offenceEntity.withCourtCase(courtCase))
                .collect(Collectors.toList());
        return courtCase.withOffences(updatedOffences);
    }
}
