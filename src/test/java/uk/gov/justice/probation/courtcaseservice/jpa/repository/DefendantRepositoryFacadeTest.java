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
    private DefendantRepository defendantRepository;

    @InjectMocks
    private DefendantRepositoryFacade defendantRepositoryFacade;

    @Test
    void shouldFetchDefendantAndPopulateOffender() {
        OffenderEntity offenderEntity = OffenderEntity.builder().crn(EntityHelper.CRN).pnc("pnc").build();
        DefendantEntity defendantEntity = EntityHelper.aDefendantEntity();
        given(defendantRepository.findFirstByDefendantId(EntityHelper.DEFENDANT_ID))
            .willReturn(Optional.of(defendantEntity));

        var actual = defendantRepositoryFacade.findFirstByDefendantId(EntityHelper.DEFENDANT_ID);

        verify(defendantRepository).findFirstByDefendantId(EntityHelper.DEFENDANT_ID);

        Assertions.assertThat(actual.get()).isEqualTo(defendantEntity.withOffender(offenderEntity));
    }

    @Test
    void shouldThroughEntityNotFoundExceptionWhenCourtCaseNotFound() {
        DefendantEntity defendantEntity = EntityHelper.aDefendantEntity();
        given(defendantRepository.findFirstByDefendantId(EntityHelper.DEFENDANT_ID))
            .willReturn(Optional.of(defendantEntity));
        assertThrows(RuntimeException.class, () ->
            defendantRepositoryFacade.findFirstByDefendantId(EntityHelper.DEFENDANT_ID));
    }
}