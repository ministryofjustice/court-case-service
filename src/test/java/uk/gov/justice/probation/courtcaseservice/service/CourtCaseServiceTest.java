package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseServiceTest {

    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "1600028912";
    private static final String COURT_CODE = "SHF";

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

    private LocalDate date = LocalDate.of(2020, 1, 16);
    private CourtCaseService service;

    @Before
    public void setup() {
        service = new CourtCaseService(courtRepository, courtCaseRepository);
        List<OffenceEntity> offences = Collections.singletonList(new OffenceEntity(null, null, "OFFENCE_TITLE", "OFFENCE_SUMMARY", "ACT", 1));
        courtCase = new CourtCaseEntity(1234L, null, CASE_ID, null, COURT_CODE, null, null, null, null, null, offences, null);
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);
    }

    @Test
    public void filterByDateShouldRetrieveCourtCasesFromRepository() {
        when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(eq(COURT_CODE), any(), any())).thenReturn(caseList);

        List<CourtCaseEntity> courtCaseEntities = service.filterCasesByCourtAndDate(COURT_CODE, date);

        assertThat(courtCaseEntities).isEqualTo(caseList);
    }

    @Test
    public void filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(null);

        var exception = catchThrowable(() ->
                service.filterCasesByCourtAndDate(COURT_CODE, date));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");

    }

    @Test
    public void getCourtCaseShouldRetrieveCaseFromRepository() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);
        when(courtCaseRepository.findByCaseNo(CASE_NO)).thenReturn(courtCase);

        service.getCaseByCaseNumber(COURT_CODE, CASE_NO);
        verify(courtCaseRepository, times(1)).findByCaseNo(CASE_NO);
    }

    @Test
    public void getCourtCaseShouldThrowNotFoundException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);
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
          when(courtCaseRepository.save(caseEntityCaptor.capture())).thenReturn(courtCase);

          service.createOrUpdateCase(CASE_ID, courtCase);
          assertThat(caseEntityCaptor.getValue().getOffences().get(0)
                  .getCourtCase()).isEqualTo(courtCase);
    }
}
