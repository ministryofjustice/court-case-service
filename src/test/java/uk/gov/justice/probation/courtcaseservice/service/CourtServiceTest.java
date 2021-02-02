package uk.gov.justice.probation.courtcaseservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {
    public static final String COURT_CODE = "B14LO";

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    private final CourtEntity leicester = CourtEntity.builder().courtCode("B33HU").name("Leicester").build();
    private final CourtEntity aberystwyth = CourtEntity.builder().courtCode("B63AD").name("Aberystwyth").build();
    private final CourtEntity sheffield = CourtEntity.builder().name("Sheffield Magistrates Court").courtCode(COURT_CODE).build();

    @Test
    void whenUpdateCourtCalled_thenCreateOrUpdateCourtInRepository() {

        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());
        when(courtRepository.save(sheffield)).thenReturn(sheffield);

        CourtEntity savedEntity = courtService.updateCourt(this.sheffield);

        assertThat(savedEntity).isSameAs(sheffield);
    }

    @Test
    void givenCourtAlreadyExists_whenCreateCourt_thenThrowException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(sheffield));

        assertThatExceptionOfType(DuplicateEntityException.class)
                .isThrownBy(() -> courtService.updateCourt(this.sheffield))
                .withMessage("Court with courtCode '" + COURT_CODE + "' already exists");
    }

    @Test
    void whenGetCourts_thenReturnAll() {

        when(courtRepository.findAll()).thenReturn(List.of(sheffield, leicester, aberystwyth));

        List<CourtEntity> courts = courtService.getCourts();

        assertThat(courts).containsExactly(aberystwyth, leicester, sheffield);
        verify(courtRepository).findAll();
    }
}
