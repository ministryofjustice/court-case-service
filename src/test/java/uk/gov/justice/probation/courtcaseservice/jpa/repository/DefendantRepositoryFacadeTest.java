package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefendantRepositoryFacadeTest {

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private DefendantRepository defendantRepository;

    @InjectMocks
    private DefendantRepositoryFacade defendantRepositoryFacade;

    @Test
    void shouldFetchDefendantAndPopulateOffender() {
        OffenderEntity offenderEntity = OffenderEntity.builder().crn(EntityHelper.CRN).pnc("pnc").build();
        given(offenderRepository.findByCrn(EntityHelper.CRN)).willReturn(Optional.ofNullable(
            offenderEntity
        ));
        DefendantEntity defendantEntity = EntityHelper.aDefendantEntity();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID))
            .willReturn(Optional.of(defendantEntity));

        var actual = defendantRepositoryFacade.findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID);

        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID);
        verify(offenderRepository).findByCrn(EntityHelper.CRN);

        Assertions.assertThat(actual.get()).isEqualTo(defendantEntity.withOffender(offenderEntity));
    }

    @Test
    void shouldThroughEntityNotFoundExceptionWhenCourtCaseNotFound() {
        DefendantEntity defendantEntity = EntityHelper.aDefendantEntity();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID))
            .willReturn(Optional.of(defendantEntity));
        given(offenderRepository.findByCrn(EntityHelper.CRN))
            .willReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            defendantRepositoryFacade.findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID));
    }
}