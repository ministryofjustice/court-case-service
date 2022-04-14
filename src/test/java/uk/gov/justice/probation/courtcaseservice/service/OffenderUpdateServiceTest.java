package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;

@ExtendWith(MockitoExtension.class)
class OffenderUpdateServiceTest {

    static String DEFENDANT_ID = "defendant-id-one";
    static String CRN = "crn-one";

    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private OffenderRepositoryFacade offenderRepositoryFacade;

    @InjectMocks
    private OffenderUpdateService offenderUpdateService;

    @Test
    void shouldReturnEmptyOffenderWhenDefendantCrnIsAbsent() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().build()));

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block())
            .withMessage("Offender details not found for defendant defendant-id-one");
    }

    @Test
    void shouldReturnEmptyOffenderWhenOffenderWithCrnDoesNoExist() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().crn(CRN).build()));

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block())
            .withMessage("Offender details not found for defendant defendant-id-one");
    }

    @Test
    void shouldReturnOffenderWhenFoundInDatabase() {
        given(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID))
                .willReturn(Optional.of(DefendantEntity.builder().crn(CRN).build()));

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.of(OffenderEntity.builder().crn(CRN)
                .preSentenceActivity(true).breach(true).suspendedSentenceOrder(true).build()));

        var offender = offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID).block();
        assertThat(offender).isEqualTo(OffenderEntity.builder().crn(CRN).breach(true).suspendedSentenceOrder(true)
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

        given(offenderRepositoryFacade.updateOffenderIfItExists(offenderEntity)).willReturn(offenderEntity);
        given(offenderRepository.save(offenderEntity)).willReturn(offenderEntity.withId(1L));

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderEntity).block();
        assertThat(offender).isEqualTo(OffenderEntity.builder().crn(CRN).breach(false).suspendedSentenceOrder(false)
                .preSentenceActivity(false).id(1L).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepositoryFacade).updateOffenderIfItExists(offenderEntity);
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

        given(offenderRepositoryFacade.updateOffenderIfItExists(offenderUpdate)).willReturn(offenderUpdate);
        given(offenderRepository.save(any(OffenderEntity.class))).willReturn(offenderUpdate);

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderUpdate).block();

        assertThat(offender).isEqualTo(OffenderEntity.builder().crn(CRN).suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepositoryFacade).updateOffenderIfItExists(offenderUpdate);
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

        given(offenderRepositoryFacade.updateOffenderIfItExists(offenderUpdate)).willReturn(offenderUpdate);
        given(offenderRepository.save(any(OffenderEntity.class))).willReturn(offenderUpdate);

        var offender = offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, offenderUpdate).block();

        assertThat(offender).isEqualTo(OffenderEntity.builder().crn(NEW_CRN).suspendedSentenceOrder(true)
                .preSentenceActivity(true).breach(false)
                .probationStatus(CURRENT).build());
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(offenderRepository).save(offenderUpdate);
        verify(defendantRepository).save(defendantEntity.withCrn(NEW_CRN));
    }
}