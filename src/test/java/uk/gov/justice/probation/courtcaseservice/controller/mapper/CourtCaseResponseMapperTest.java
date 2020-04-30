package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 2, 26);
    private static final String DEFENDANT_SEX = "DEFENDANT_SEX";
    private static final String NATIONALITY_1 = "NATIONALITY_1";
    private static final String NATIONALITY_2 = "NATIONALITY_2";
    private static final CourtSession SESSION = CourtSession.MORNING;
    private CourtCaseEntity courtCaseEntity;
    private List<OffenceEntity> offences;
    private CourtCaseResponseMapper courtCaseResponseMapper = new CourtCaseResponseMapper();
    private AddressPropertiesEntity addressPropertiesEntity = new AddressPropertiesEntity("27", "Elm Place", "ad21 5dr", "Bangor", null, null);


    @Before
    public void setUp() {
        offences = Arrays.asList(
                new OffenceEntity(null, null, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, 1),
                new OffenceEntity(null, null, OFFENCE_TITLE + "2", OFFENCE_SUMMARY + "2", ACT + "2", 2)
        );
        courtCaseEntity = buildCourtCaseEntity(offences);
    }

    @Test
    public void shouldMapEntityToResponse() {
        var courtCaseResponse = courtCaseResponseMapper.mapFrom(courtCaseEntity);

        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(courtCaseResponse.getLastUpdated()).isEqualTo(LAST_UPDATED);
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
        assertThat(courtCaseResponse.getListNo()).isEqualTo(LIST_NO);
        assertThat(courtCaseResponse.getDefendantDob()).isEqualTo(DEFENDANT_DOB);
        assertThat(courtCaseResponse.getDefendantSex()).isEqualTo(DEFENDANT_SEX);
        assertThat(courtCaseResponse.getNationality1()).isEqualTo(NATIONALITY_1);
        assertThat(courtCaseResponse.getNationality2()).isEqualTo(NATIONALITY_2);


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
        var reorderedOffences = Arrays.asList(offences.get(1), offences.get(0));
        var reorderedCourtCaseEntity = buildCourtCaseEntity(reorderedOffences);

        var courtCaseResponse = courtCaseResponseMapper.mapFrom(reorderedCourtCaseEntity);

        var firstOffence = courtCaseResponse.getOffences().get(0);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);

        var secondOffence = courtCaseResponse.getOffences().get(1);
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");

    }

    @Test
    public void shouldTolerateMissingSequenceNumbersInResponseOrdering() {
        var unorderedOffences = Arrays.asList(
            new OffenceEntity(null, null, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, null),
            new OffenceEntity(null, null, OFFENCE_TITLE + "2", OFFENCE_SUMMARY + "2", ACT + "2", null)
        );
        var unorderedCourtCaseEntity = buildCourtCaseEntity(unorderedOffences);

        var courtCaseResponse = courtCaseResponseMapper.mapFrom(unorderedCourtCaseEntity);

        var firstOffence = courtCaseResponse.getOffences().get(0);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);

        var secondOffence = courtCaseResponse.getOffences().get(1);
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");

    }

    private CourtCaseEntity buildCourtCaseEntity(List<OffenceEntity> offences) {
        return new CourtCaseEntity(ID, LAST_UPDATED, CASE_ID, CASE_NO, COURT_CODE, COURT_ROOM, SESSION_START_TIME, PROBATION_STATUS, PREVIOUSLY_KNOWN_TERMINATION_DATE, SUSPENDED_SENTENCE_ORDER, BREACH, offences, DEFENDANT_NAME, addressPropertiesEntity, CRN, PNC, LIST_NO, DEFENDANT_DOB, DEFENDANT_SEX, NATIONALITY_1, NATIONALITY_2);
    }
}
