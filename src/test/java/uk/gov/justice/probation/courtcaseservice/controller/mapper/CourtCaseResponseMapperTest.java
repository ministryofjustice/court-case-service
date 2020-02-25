package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
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
    private static final String DATA = "DATA";
    private static final LocalDateTime LAST_UPDATED = LocalDateTime.of(2020, 2, 24, 1, 0);
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 25, 1, 0);
    private static final LocalDate PREVIOUSLY_KNOWN_TERMINATION_DATE = LocalDate.of(2020, 2, 26);
    private static final String OFFENCE_TITLE = "OFFENCE_TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE_SUMMARY";
    private static final String ACT = "ACT";
    private CourtCaseEntity courtCaseEntity;

    @Before
    public void setUp() {
        List<OffenceEntity> offences = Arrays.asList(
                new OffenceEntity(null, null, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, 1),
                new OffenceEntity(null, null, OFFENCE_TITLE + "2", OFFENCE_SUMMARY + "2", ACT + "2", 2));
        courtCaseEntity = new CourtCaseEntity(ID, LAST_UPDATED, CASE_ID, CASE_NO, COURT_CODE, COURT_ROOM, SESSION_START_TIME, PROBATION_STATUS, PREVIOUSLY_KNOWN_TERMINATION_DATE, SUSPENDED_SENTENCE_ORDER, offences, DATA);
    }

    @Test
    public void shouldMapEntityToResponse() {
        CourtCaseResponse courtCaseResponse = new CourtCaseResponseMapper().mapFrom(courtCaseEntity);

        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(courtCaseResponse.getData()).isEqualTo(DATA);
        assertThat(courtCaseResponse.getLastUpdated()).isEqualTo(LAST_UPDATED);
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUSLY_KNOWN_TERMINATION_DATE);
        assertThat(courtCaseResponse.getProbationStatus()).isEqualTo(PROBATION_STATUS);
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(courtCaseResponse.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
    }

    @Test
    public void shouldMapOffencesToResponse() {
        CourtCaseResponse courtCaseResponse = new CourtCaseResponseMapper().mapFrom(courtCaseEntity);

        assertThat(courtCaseResponse.getOffences().size()).isEqualTo(2);

        OffenceResponse firstOffence = courtCaseResponse.getOffences().get(0);

        assertThat(firstOffence.getAct()).isEqualTo(ACT);
        assertThat(firstOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(firstOffence.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);


        OffenceResponse secondOffence = courtCaseResponse.getOffences().get(1);

        assertThat(secondOffence.getAct()).isEqualTo(ACT + "2");
        assertThat(secondOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE + "2");
        assertThat(secondOffence.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY + "2");
    }

    // TODO: test that offence sequence number is reflected in ordering

}