package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseServiceTest {

    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "1600028912";
    private static final String COURT_CODE = "SHF";
    private static final String BAD_CASE_ID = "BAD_CASE_ID";
    private static final String COURT_ROOM = "COURT_ROOM";
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 26, 9, 0);
    private static final String PROBATION_STATUS = "PROBATION_STATUS";
    private static final LocalDate TERMINATION_DATE = LocalDate.of(2020, 2, 27);
    private static final LocalDate SEARCH_DATE = LocalDate.of(2020, 1, 16);
    private static final LocalDateTime LAST_UPDATED = LocalDateTime.of(2020, 2, 25, 9, 0);
    private static final boolean SUSPENDED_SENTENCE = true;
    private static final boolean BREACH = true;
    private static final String DEFENDANT_NAME = "JTEST";
    private static final AddressPropertiesEntity DEFENDANT_ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "AB21 3ES", "Bangor", null, null);
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 12, 14);
    private static final String DEFENDANT_SEX = "M";
    private static final String NATIONALITY_1 = "British";
    private static final String NATIONALITY_2 = "Polish";

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtCaseRepository courtCaseRepository;

    @Mock
    private CourtEntity courtEntity;

    @Mock
    private List<CourtCaseEntity> caseList;

    @Captor
    private ArgumentCaptor<CourtCaseEntity> caseEntityCaptor;

    private CourtCaseEntity courtCase;

    private CourtCaseService service;

    @Before
    public void setup() {
        service = new CourtCaseService(courtRepository, courtCaseRepository);
        List<OffenceEntity> offences = Collections.singletonList(new OffenceEntity(null, null, "OFFENCE_TITLE", "OFFENCE_SUMMARY", "ACT", 1));
        courtCase = new CourtCaseEntity(1234L, LAST_UPDATED, CASE_ID, CASE_NO, COURT_CODE, COURT_ROOM, SESSION_START_TIME, PROBATION_STATUS, TERMINATION_DATE, SUSPENDED_SENTENCE, BREACH, offences, DEFENDANT_NAME, DEFENDANT_ADDRESS, CRN, PNC, LIST_NO, DEFENDANT_DOB, DEFENDANT_SEX, NATIONALITY_1, NATIONALITY_2);

        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);
        when(courtCaseRepository.save(caseEntityCaptor.capture())).thenReturn(courtCase);
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);
    }

    @Test
    public void filterByDateShouldRetrieveCourtCasesFromRepository() {
        when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(eq(COURT_CODE), any(), any())).thenReturn(caseList);

        List<CourtCaseEntity> courtCaseEntities = service.filterCasesByCourtAndDate(COURT_CODE, SEARCH_DATE);

        assertThat(courtCaseEntities).isEqualTo(caseList);
    }

    @Test
    public void filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(null);

        var exception = catchThrowable(() ->
                service.filterCasesByCourtAndDate(COURT_CODE, SEARCH_DATE));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");

    }

    @Test
    public void getCourtCaseShouldRetrieveCaseFromRepository() {
        when(courtCaseRepository.findByCaseNo(CASE_NO)).thenReturn(courtCase);

        service.getCaseByCaseNumber(COURT_CODE, CASE_NO);
        verify(courtCaseRepository, times(1)).findByCaseNo(CASE_NO);
    }

    @Test
    public void getCourtCaseShouldThrowNotFoundException() {
        when(courtCaseRepository.findByCaseNo(CASE_NO)).thenReturn(null);

        var exception = catchThrowable(() ->
                service.getCaseByCaseNumber(COURT_CODE, CASE_NO)
        );
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + CASE_NO + " not found");

    }

    @Test
    public void getCourtCaseShouldThrowIncorrectCourtException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(null);

        var exception = catchThrowable(() ->
                service.getCaseByCaseNumber(COURT_CODE, CASE_NO)
        );
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");

    }

    @Test
    public void createOrUpdateCaseShouldAmendOffencesWithCaseId() {

          service.createOrUpdateCase(CASE_ID, courtCase);
          assertThat(caseEntityCaptor.getValue().getOffences().get(0)
                  .getCourtCase()).isEqualTo(courtCase);
    }

    @Test
    public void createOrUpdateCaseShouldThrowExceptionIfCourtDoesNotExist() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(null);
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy( () ->
                service.createOrUpdateCase(CASE_ID, courtCase)
        ).withMessage("Court " + COURT_CODE + " not found");
    }

    @Test
    public void createOrUpdateCaseShouldThrowExceptionIfCaseIdsDontMatch() {
        courtCase.setCaseId(BAD_CASE_ID);
        assertThatExceptionOfType(InputMismatchException.class).isThrownBy( () ->
                service.createOrUpdateCase(CASE_ID, courtCase)
        ).withMessage("Case ID " + CASE_ID + " does not match with " + BAD_CASE_ID);
    }

    @Test
    public void createOrUpdateCase_shouldUpdateExistingRecordIfItExists() {
        var existingCourtCase = new CourtCaseEntity();
        existingCourtCase.setCaseId(CASE_ID);
        when(courtCaseRepository.findByCaseId(CASE_ID)).thenReturn(existingCourtCase);
        service.createOrUpdateCase(CASE_ID, courtCase);

        assertThat(existingCourtCase.getCaseId()).isEqualTo(CASE_ID);
        assertThat(existingCourtCase.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(existingCourtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(existingCourtCase.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(existingCourtCase.getProbationStatus()).isEqualTo(PROBATION_STATUS);
        assertThat(existingCourtCase.getPreviouslyKnownTerminationDate()).isEqualTo(TERMINATION_DATE);
        assertThat(existingCourtCase.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(existingCourtCase.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE);
        assertThat(existingCourtCase.getBreach()).isEqualTo(BREACH);
        assertThat(existingCourtCase.getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(existingCourtCase.getDefendantAddress()).isEqualTo(DEFENDANT_ADDRESS);

        verify(courtCaseRepository).save(existingCourtCase);
    }

    @Test
    public void createOrUpdateCase_shouldCreateANewRecordIfNoneExists() {
        when(courtCaseRepository.findByCaseId(CASE_ID)).thenReturn(null);
        service.createOrUpdateCase(CASE_ID, courtCase);

        verify(courtCaseRepository).save(courtCase);
    }
}
