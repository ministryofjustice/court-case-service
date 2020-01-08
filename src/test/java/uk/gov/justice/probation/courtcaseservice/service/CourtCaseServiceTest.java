package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseServiceTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;

    private final String caseNo = "1600028912";

    private final String courtCode = "SHF";

    private CourtCaseService service;


    @Before
    public void setup() {
        service = new CourtCaseService(courtCaseRepository);
    }


    @Test
    public void getCourtCaseShouldRetrieveCaseFromRepository() {
        var courtCase = mock(CourtCaseEntity.class);
        when(courtCaseRepository.findByCaseNo(caseNo)).thenReturn(courtCase);
        service.getCaseByCaseNumber(courtCode, caseNo);
        verify(courtCaseRepository,times(1)).findByCaseNo(caseNo);
    }


    @Test
    public void getCourtCaseShouldThrowNotFoundException() {
        when(courtCaseRepository.findByCaseNo(caseNo)).thenReturn(null);
        var exception = catchThrowable(() -> { service.getCaseByCaseNumber(courtCode, caseNo); });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + caseNo   + " not found");

    }

}
