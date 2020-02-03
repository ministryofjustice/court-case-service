package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtServiceTest {
    public static final String COURT_CODE = "SHF";
    @Mock
    private CourtRepository courtRepository;

    private CourtService courtService;
    private CourtEntity courtEntity = new CourtEntity(12345L, "Sheffield Magistrates Court", COURT_CODE);

    @Before
    public void setUp() {
        courtService = new CourtService(courtRepository);
        when(courtRepository.save(courtEntity)).thenReturn(courtEntity);
    }

    @Test
    public void whenUpdateCourtCalled_thenCreateOrUpdateCourtInRepository() {

        CourtEntity savedEntity = courtService.updateCourt(this.courtEntity);

        assertThat(savedEntity).isEqualTo(courtEntity);
    }

    @Test
    public void givenCourtAlreadyExists_whenCreateCourt_thenThrowException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(courtEntity);

        assertThatExceptionOfType(DuplicateEntityException.class)
                .isThrownBy(() -> courtService.updateCourt(this.courtEntity))
                .withMessage("Court with courtCode 'SHF' already exists");
    }
}