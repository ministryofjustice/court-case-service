package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderEntityInitService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.PREVIOUSLY_KNOWN;

@ExtendWith(MockitoExtension.class)
class OffenderRepositoryFacadeTest {

    private static final String CRO = "CRO007";
    private static final String PNC = "PNC007";

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderEntityInitService offenderEntityInitService;

    @Spy
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
            .cro(CRO)
            .pnc(PNC)
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
    void shouldSaveOffenderWhenExists() {

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
        given(offenderRepository.save(updatedOffender)).willReturn(updatedOffender);

        final var actual = offenderRepositoryFacade.save(updatedOffender);

        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepositoryFacade).updateOffenderIfItExists(updatedOffender);
        verify(offenderRepository).save(updatedOffender);
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

    @Test
    void givenOffenderExist_upsert_shouldUpdate() {

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
            .id(1L)
            .crn(CRN)
            .breach(false)
            .awaitingPsr(true)
            .probationStatus(CURRENT)
            .previouslyKnownTerminationDate(now.plusDays(20))
            .suspendedSentenceOrder(true)
            .preSentenceActivity(false)
            .build();

        given(offenderEntityInitService.findByCrn(CRN)).willReturn(Optional.of(existingOffender));
        given(offenderRepository.save(existingOffender)).willReturn(existingOffender);

        final var actual = offenderRepositoryFacade.upsertOffender(updatedOffender);

        verify(offenderEntityInitService).findByCrn(CRN);
        verify(offenderRepository).save(existingOffender);
        assertThat(existingOffender).isEqualTo(updatedOffender.withId(1L));
        assertThat(actual).isEqualTo(existingOffender);
    }

    @Test
    void givenOffenderDoesNotExist_saveInputOffender() {

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

        given(offenderEntityInitService.findByCrn(CRN)).willReturn(Optional.empty());

        final var actual = offenderRepositoryFacade.upsertOffender(updatedOffender);
        verify(offenderEntityInitService).findByCrn(CRN);
        verify(offenderRepository).save(updatedOffender);
    }
    @Test
    void givenOffenderExist_offenderDetailsDontChange_doNotSave() {

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

        given(offenderEntityInitService.findByCrn(CRN)).willReturn(Optional.of(updatedOffender));

        final var actual = offenderRepositoryFacade.upsertOffender(updatedOffender);
        verify(offenderEntityInitService).findByCrn(CRN);
        verifyNoMoreInteractions(offenderRepository);
    }
}