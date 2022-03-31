package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.AssertionErrors;
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantOffender;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.PREVIOUSLY_KNOWN;

@ExtendWith(MockitoExtension.class)
class OffenderUpdateServiceTest {

    static String DEFENDANT_ID = "defendant-id-one";
    static String CRN = "crn-one";

    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private OffenderRepository offenderRepository;

    @InjectMocks
    private OffenderUpdateService offenderUpdateService;

    @Test
    void shouldReturnEmptyOffenderWhenDefendantCrnIsAbsent() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().build()));

        var offender = offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block();
        assertThat(offender).isEqualTo(DefendantOffender.builder().build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verifyNoInteractions(offenderRepository);
    }

    @Test
    void shouldReturnEmptyOffenderWhenOffenderWithCrnDoesNoExist() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().crn(CRN).build()));

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.ofNullable(null));

        var offender = offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block();
        assertThat(offender).isEqualTo(DefendantOffender.builder().build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).findByCrn(CRN);
    }

    @Test
    void shouldReturnOffenderWhenFoundInDatabase() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().crn(CRN).build()));

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.of(OffenderEntity.builder().crn(CRN)
                .preSentenceActivity(true).breach(true).suspendedSentenceOrder(true).build()));

        var offender = offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block();
        assertThat(offender).isEqualTo(DefendantOffender.builder().crn(CRN).breach(true).suspendedSentenceOrder(true)
                .preSentenceActivity(true).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).findByCrn(CRN);
    }

    @Test
    void shouldThrowEntityNotFoundWhenDefendantDoesNotExistInRepositoryForGetOffenderRequest() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.ofNullable(null));

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID);
        });

        String expectedMessage = String.format("Defendant with id %s does not exist", DEFENDANT_ID);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
    }

    @Test
    void shouldThrowEntityNotFoundWhenDefendantDoesNotExistInRepositoryForGetRemoveOffenderRequest() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.ofNullable(null));

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            offenderUpdateService.removeDefendantOffenderAssociation(DEFENDANT_ID);
        });

        String expectedMessage = String.format("Defendant with id %s does not exist", DEFENDANT_ID);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
    }

    @Test
    void shouldRemoveDefendantOffenderAssociationWhenCrnPresentOnDefendant() {
        DefendantEntity defendantEntity = DefendantEntity.builder().crn(CRN).build();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(defendantEntity));

        offenderUpdateService.removeDefendantOffenderAssociation(DEFENDANT_ID);

        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).save(defendantEntity.withCrn(null));
    }

    @Test
    void shouldNotSaveDefendantIfDefendantWasNotAssociatedAlreadyOnRemoveOffenderRequest() {
        DefendantEntity defendantEntity = DefendantEntity.builder().build();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(defendantEntity));

        offenderUpdateService.removeDefendantOffenderAssociation(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository, times(0)).save(defendantEntity.withCrn(null));
    }

    @Test
    void shouldThrowEntityNotFoundWhenDefendantDoesNotExistInRepositoryForUpdateOffenderRequest() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.ofNullable(null));

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, OffenderEntity.builder().build());
        });

        String expectedMessage = String.format("Defendant with id %s does not exist", DEFENDANT_ID);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
    }

    @Test
    void shouldCreateOffenderWhenOffenderDoesNotExistAndAssociateWithDefendant() {
        DefendantEntity defendantEntity = DefendantEntity.builder().build();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(defendantEntity));
        OffenderEntity offenderEntity = OffenderEntity.builder().crn(CRN).build();

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.ofNullable(null));
        given(offenderRepository.save(offenderEntity)).willReturn(offenderEntity.withId(1L));

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderEntity).block();
        assertThat(offender).isEqualTo(DefendantOffender.builder().crn(CRN).breach(false).suspendedSentenceOrder(false)
                .preSentenceActivity(false).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).save(offenderEntity);
        verify(defendantRepository).save(defendantEntity.withCrn(CRN));
    }

    @Test
    void shouldUpdateOffenderWhenOffenderExistAndAssociateWithDefendantGivenNotAlreadyAssociated() {
        DefendantEntity defendantEntity = DefendantEntity.builder().build();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(defendantEntity));

        OffenderEntity offenderUpdate = OffenderEntity.builder().suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT).crn(CRN).build();

        OffenderEntity existingOffender = OffenderEntity.builder().suspendedSentenceOrder(false)
                .preSentenceActivity(false).breach(false)
                .probationStatus(PREVIOUSLY_KNOWN).crn(CRN).build();

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.ofNullable(existingOffender));
        given(offenderRepository.save(any(OffenderEntity.class))).willReturn(offenderUpdate);

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderUpdate).block();

        assertThat(offender).isEqualTo(DefendantOffender.builder().crn(CRN).suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT.name()).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).save(offenderUpdate);
        verify(defendantRepository).save(defendantEntity.withCrn(CRN));
    }

    @Test
    void shouldUpdateOffenderWhenOffenderExistAndAssociateWithDefendantGivenDefendantAssociatedWithDifferentOffender() {
        DefendantEntity defendantEntity = DefendantEntity.builder().crn(CRN).build();
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(defendantEntity));

        String NEW_CRN = "NEW_CRN";

        OffenderEntity offenderUpdate = OffenderEntity.builder().suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT).crn(NEW_CRN).build();

        OffenderEntity existingOffender = OffenderEntity.builder().suspendedSentenceOrder(false)
                .preSentenceActivity(false).breach(false)
                .probationStatus(PREVIOUSLY_KNOWN).crn(NEW_CRN).build();

        given(offenderRepository.findByCrn(NEW_CRN)).willReturn(Optional.ofNullable(existingOffender));
        given(offenderRepository.save(any(OffenderEntity.class))).willReturn(offenderUpdate);

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderUpdate).block();

        assertThat(offender).isEqualTo(DefendantOffender.builder().crn(NEW_CRN).suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT.name()).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).findByCrn(NEW_CRN);
        verify(offenderRepository).save(offenderUpdate);
        verify(defendantRepository).save(defendantEntity.withCrn(NEW_CRN));
    }
}