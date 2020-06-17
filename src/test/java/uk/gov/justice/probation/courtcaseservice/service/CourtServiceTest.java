package uk.gov.justice.probation.courtcaseservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
    public static final String COURT_CODE = "SHF";
    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    private CourtEntity courtEntity;

    @BeforeEach
    void beforeEach() {
        courtEntity = CourtEntity.builder()
            .id(12345L)
            .name("Sheffield Magistrates Court")
            .courtCode(COURT_CODE)
            .build();
    }

    @Test
    void whenUpdateCourtCalled_thenCreateOrUpdateCourtInRepository() {

        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());
        when(courtRepository.save(courtEntity)).thenReturn(courtEntity);

        CourtEntity savedEntity = courtService.updateCourt(this.courtEntity);

        assertThat(savedEntity).isSameAs(courtEntity);
    }

    @Test
    void givenCourtAlreadyExists_whenCreateCourt_thenThrowException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));

        assertThatExceptionOfType(DuplicateEntityException.class)
                .isThrownBy(() -> courtService.updateCourt(this.courtEntity))
                .withMessage("Court with courtCode 'SHF' already exists");
    }
}
