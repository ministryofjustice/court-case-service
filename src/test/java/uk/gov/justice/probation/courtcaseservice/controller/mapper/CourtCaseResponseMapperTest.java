package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CourtCaseResponseMapperTest {

    public static final long ID = 1234L;
    public static final String CASE_ID = "CASE_ID";
    public static final String CASE_NO = "CASE_NO";
    public static final String COURT_CODE = "COURT_CODE";
    public static final String COURT_ROOM = "COURT_ROOM";
    public static final String PROBATION_STATUS = "PROBATION_STATUS";
    public static final boolean SUSPENDED_SENTENCE_ORDER = true;
    public static final String DATA = "DATA";
    public static final LocalDateTime LAST_UPDATED = LocalDateTime.of(2020, 2, 24, 1, 0);
    public static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 25, 1, 0);
    public static final LocalDate PREVIOUSLY_KNOWN_TERMINATION_DATE = LocalDate.of(2020, 2, 26);

    @Test
    public void shouldMapEntityToResponse() {
        List<OffenceEntity> offences = Collections.emptyList();
        CourtCaseEntity courtCaseEntity = new CourtCaseEntity(ID, LAST_UPDATED, CASE_ID, CASE_NO, COURT_CODE, COURT_ROOM, SESSION_START_TIME, PROBATION_STATUS, PREVIOUSLY_KNOWN_TERMINATION_DATE, SUSPENDED_SENTENCE_ORDER, offences, DATA);

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
        assertThat(courtCaseResponse.getOffences()).isEqualTo(offences);

    }

}