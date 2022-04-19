package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.PREVIOUSLY_KNOWN;

@ExtendWith(MockitoExtension.class)
class OffenderRepositoryFacadeTest {

    @Mock
    private OffenderRepository offenderRepository;

    @InjectMocks
    private OffenderRepositoryFacade offenderRepositoryFacade;

    @Test
    void shouldUpdateOffenderWhenExists() {

        LocalDate now = LocalDate.now();
        final var CRN = "CRN001";
        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .breach(true)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(now.plusDays(30))
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        final var existingOffender = OffenderEntity.builder()
            .crn(CRN)
            .breach(false)
            .awaitingPsr(true)
            .probationStatus(CURRENT)
            .previouslyKnownTerminationDate(now.plusDays(20))
            .suspendedSentenceOrder(true)
            .preSentenceActivity(false)
            .build();

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.of(existingOffender));

        final var actual = offenderRepositoryFacade.updateOffenderIfItExists(updatedOffender);

        verify(offenderRepository).findByCrn(CRN);
        assertThat(existingOffender).isEqualTo(updatedOffender);
        assertThat(actual).isEqualTo(updatedOffender);
    }

    @Test
    void shouldReturnInputUpdatedOffenderWhenOffenderDoesNotExists() {

        LocalDate now = LocalDate.now();
        final var CRN = "CRN001";
        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .breach(true)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(now.plusDays(30))
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.empty());

        final var actual = offenderRepositoryFacade.updateOffenderIfItExists(updatedOffender);
        verify(offenderRepository).findByCrn(CRN);
        assertThat(actual).isEqualTo(updatedOffender);
    }
}