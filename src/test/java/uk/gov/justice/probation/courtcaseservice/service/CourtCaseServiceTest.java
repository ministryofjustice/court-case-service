package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtCaseRepository courtCaseRepository;

    private final String caseNo = "1600028912";
    private final Long caseId = 123456L;
    private final String courtCode = "SHF";
    private CourtCaseService service;

    @Before
    public void setup() {
        service = new CourtCaseService(courtRepository, courtCaseRepository);
    }

    @Test
    public void getCourtCaseShouldRetrieveCaseFromRepository() {
        CourtEntity courtEntity = mock(CourtEntity.class);
        CourtCaseEntity courtCase = mock(CourtCaseEntity.class);
        when(courtRepository.findByCourtCode(courtCode)).thenReturn(courtEntity);
        when(courtCaseRepository.findByCaseNo(caseNo)).thenReturn(courtCase);

        service.getCaseByCaseNumber(courtCode, caseNo);
        verify(courtCaseRepository, times(1)).findByCaseNo(caseNo);
    }

    @Test
    public void getCourtCaseShouldThrowNotFoundException() {
        CourtEntity courtEntity = mock(CourtEntity.class);
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

    @Test
    public void putCourtCaseShouldCreateCase() {
        CourtCaseEntity courtCase = mock(CourtCaseEntity.class);
        when(courtCaseRepository.findByCaseId(caseId)).thenReturn(courtCase);

        service.createOrUpdateCase(caseId, courtCase);
        verify(courtCaseRepository, times(1)).findByCaseId(caseId);
        verify(courtCaseRepository, times(1)).save(courtCase);
    }

}
