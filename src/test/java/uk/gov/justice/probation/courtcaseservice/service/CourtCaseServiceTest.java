package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtCaseRepository courtCaseRepository;

    @Mock
    private CourtEntity courtEntity;

    @Mock
    private CourtCaseEntity courtCase;

    @Mock
    private List<CourtCaseEntity> caseList;

    private LocalDate date = LocalDate.of(2020, 1, 16);

    private final String caseNo = "1600028912";
    private final Long caseId = 123456L;
    private final String courtCode = "SHF";
    private final Long courtId = 67890L;
    private CourtCaseService service;

    @Before
    public void setup() {
        service = new CourtCaseService(courtRepository, courtCaseRepository);
    }

    @Test
    public void filterByDateShouldRetrieveCourtCasesFromRepository() {
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(courtEntity);
        when(courtEntity.getId()).thenReturn(courtId);
        when(courtCaseRepository.findByCourtIdAndSessionStartTime(eq(courtId), any())).thenReturn(caseList);

        List<CourtCaseEntity> courtCaseEntities = service.filterCasesByCourtAndDate(courtCode, date);

        assertThat(courtCaseEntities).isEqualTo(caseList);
    }

    @Test
    public void filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(null);

        var exception = catchThrowable(() ->
                service.filterCasesByCourtAndDate(courtCode, date));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + courtCode + " not found");

    }

    @Test
    public void getCourtCaseShouldRetrieveCaseFromRepository() {
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(courtEntity);
        when(courtCaseRepository.findByCaseNo(caseNo)).thenReturn(courtCase);

        service.getCaseByCaseNumber(courtCode, caseNo);
        verify(courtCaseRepository, times(1)).findByCaseNo(caseNo);
    }

    @Test
    public void getCourtCaseShouldThrowNotFoundException() {
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(courtEntity);
        when(courtCaseRepository.findByCaseNo(caseNo)).thenReturn(null);

        var exception = catchThrowable(() -> {
            service.getCaseByCaseNumber(courtCode, caseNo);
        });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + caseNo + " not found");

    }

    @Test
    public void getCourtCaseShouldThrowIncorrectCourtException() {
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(null);

        var exception = catchThrowable(() -> {
            service.getCaseByCaseNumber(courtCode, caseNo);
        });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + courtCode + " not found");

    }
}
