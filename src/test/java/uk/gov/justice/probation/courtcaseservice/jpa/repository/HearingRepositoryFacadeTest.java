package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.NOT_SENTENCED;

@ExtendWith(MockitoExtension.class)
class HearingRepositoryFacadeTest {

    private static final String COURT_CODE = "courtCode";
    private static final String CASE_NO = "caseNo";
    private static final LocalDate A_DATE = LocalDate.of(2022, 3, 11);
    private static final LocalDateTime A_DATETIME = LocalDateTime.of(A_DATE, LocalTime.MIDNIGHT);
    private static final String HEARING_ID = "hearingId";
    private static final String DEFENDANT_ID = "defendantId";
    private static final String CRN = "12345";
    private static final OffenderEntity OFFENDER = OffenderEntity.builder()
            .crn(CRN)
            .breach(true)
            .build();
    private static final DefendantEntity DEFENDANT = DefendantEntity.builder()
            .defendantId(DEFENDANT_ID)
            .offender(OFFENDER)
            .build();
    private static final CourtCaseEntity COURT_CASE = CourtCaseEntity.builder()
            .caseId("caseId")
            .build();
    private static final HearingEntity HEARING = HearingEntity.builder()
            .hearingId(HEARING_ID)
            .courtCase(COURT_CASE)
            .hearingDefendants(List.of(HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID)
                    .defendant(DEFENDANT)
                    .build()))
            .build();
    private static final String DEFENDANT_ID_2 = "OTHER_DEFENDANT_ID";
    private static final String CRN_2 = "67890";
    private static final OffenderEntity OFFENDER_2 = OffenderEntity.builder()
            .crn(CRN_2)
            .build();
    private static final DefendantEntity DEFENDANT_2 = DEFENDANT
            .withDefendantId(DEFENDANT_ID_2)
            .withOffender(OFFENDER_2);
    private static final HearingEntity HEARING_WITH_MULTIPLE_DEFENDANTS = HEARING.withHearingDefendants(List.of(HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID_2)
                    .defendant(DEFENDANT_2)
                    .build(),
            HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID)
                    .defendant(DEFENDANT)
                    .build()));

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private OffenderRepositoryFacade offenderRepositoryFacade;
    @Mock
    private DefendantRepository defendantRepository;

    @Captor
    private ArgumentCaptor<HearingEntity> hearingCaptor;

    @InjectMocks
    private HearingRepositoryFacade facade;

    @Test
    void whenFindFirstByHearingIdOrderByIdDesc_thenReturnDefendants() {
        when(hearingRepository.findFirstByHearingIdOrderByIdDesc(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));
        final var actual = facade.findFirstByHearingIdOrderByIdDesc(HEARING_ID);

        assertThat(actual).get().isEqualTo(HEARING);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID_2).getDefendant()).isEqualTo(DEFENDANT_2);

        verify(hearingRepository).findFirstByHearingIdOrderByIdDesc(HEARING_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCourtCodeAndCaseNo_thenReturnDefendants() {
        when(hearingRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);

        assertThat(actual).get().isEqualTo(HEARING_WITH_MULTIPLE_DEFENDANTS);
        verify(hearingRepository).findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCaseId_thenReturnDefendants() {
        when(hearingRepository.findFirstByHearingIdOrderByIdDesc(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCaseId(HEARING_ID);
        assertThat(actual).get().isEqualTo(HEARING_WITH_MULTIPLE_DEFENDANTS);
        verify(hearingRepository).findFirstByHearingIdOrderByIdDesc(HEARING_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCaseIdAndDefendantId_thenReturnAHearingWithDefendant() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));

        final var actual = facade.findByCaseIdAndDefendantId(HEARING_ID, DEFENDANT_ID);
        assertThat(actual).get().isEqualTo(HEARING);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
    }

    @Test
    void whenFindByHearingIdAndDefendantId_thenReturnAHearingWithDefendant() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));

        final var actual = facade.findByHearingIdAndDefendantId(HEARING_ID, DEFENDANT_ID);
        assertThat(actual).get().isEqualTo(HEARING);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
    }

    @Test
    void givenMultipleDefendants_whenFindByCaseIdAndDefendantId_thenReturnAHearingWithAllDefendants() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCaseIdAndDefendantId(HEARING_ID, DEFENDANT_ID);
        assertThat(actual).get().isEqualTo(HEARING);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID_2).getDefendant()).isEqualTo(DEFENDANT_2);
    }

    @Test
    void givenMultipleDefendants_whenFindByHearingIdAndDefendantId_thenReturnAHearingWithAllDefendants() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByHearingIdAndDefendantId(HEARING_ID, DEFENDANT_ID);
        assertThat(actual).get().isEqualTo(HEARING);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get().getHearingDefendant(DEFENDANT_ID_2).getDefendant()).isEqualTo(DEFENDANT_2);
    }

    @Test
    void givenDefendantIdNotOnCase_whenFindByCaseIdAndDefendantId_thenReturnEmpty() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        assertThat(facade.findByCaseIdAndDefendantId(HEARING_ID, "THE_WRONG_DEFENDANT_ID")).isEmpty();
    }

    @Test
    void givenDefendantIdNotOnCase_whenFindByHearingIdAndDefendantId_thenReturnEmpty() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        assertThat(facade.findByHearingIdAndDefendantId(HEARING_ID, "THE_WRONG_DEFENDANT_ID")).isEmpty();
    }

    @Test
    void givenDefendantOnCase_andDefendantDoesNotExist_whenFindByCaseIdAndDefendantId_thenThrowException() {
        HEARING.getHearingDefendant(DEFENDANT_ID).setHearing(HEARING);
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> facade.findByCaseIdAndDefendantId(HEARING_ID, DEFENDANT_ID))
                .withMessage("Unexpected state: Defendant 'defendantId' is specified on hearing 'hearingId' but it does not exist");

    }    @Test
    void givenDefendantOnCase_andDefendantDoesNotExist_whenFindByHearingIdAndDefendantId_thenThrowException() {
        HEARING.getHearingDefendant(DEFENDANT_ID).setHearing(HEARING);
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> facade.findByHearingIdAndDefendantId(HEARING_ID, DEFENDANT_ID))
                .withMessage("Unexpected state: Defendant 'defendantId' is specified on hearing 'hearingId' but it does not exist");
    }

    @Test
    void givenDefendantOnCase_andDefendantDoesNotExist_andHearingIdIsNotAvailable_whenFindByCaseIdAndDefendantId_thenThrowExceptionWithoutHearingIdAsFallback() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> facade.findByCaseIdAndDefendantId(HEARING_ID, DEFENDANT_ID))
                .withMessage("Unexpected state: Defendant 'defendantId' is specified on hearing '<Error: Unable to determine hearingId>' but it does not exist");
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_andDateTimeIsMinMax_thenCallRepoMethodWithoutDateConstraints() {
        when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, LocalDateTime.MIN, LocalDateTime.MAX);
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get(1).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT_2);
        assertThat(actual.get(1).getHearingDefendants().get(1).getDefendant()).isEqualTo(DEFENDANT);

        verify(hearingRepository).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE);
        verify(defendantRepository, times(2)).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_andDateTimesAreNull_thenCallRepoMethodWithoutDateConstraints() {
        when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, null, null);
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get(1).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT_2);
        assertThat(actual.get(1).getHearingDefendants().get(1).getDefendant()).isEqualTo(DEFENDANT);

        verify(hearingRepository).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE);
        verify(defendantRepository, times(2)).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_thenReturnDefendants() {
        when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT_2));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME);
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT);
        assertThat(actual.get(1).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT_2);
        assertThat(actual.get(1).getHearingDefendants().get(1).getDefendant()).isEqualTo(DEFENDANT);

        verify(hearingRepository).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME);
        verify(defendantRepository, times(2)).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void findLastModifiedByHearingDay() {
        when(hearingRepository.findLastModifiedByHearingDay(COURT_CODE, A_DATE)).thenReturn(Optional.of(A_DATETIME));
        final var actual = facade.findLastModifiedByHearingDay(COURT_CODE, A_DATE);
        assertThat(actual).get().isEqualTo(A_DATETIME);
        verify(hearingRepository).findLastModifiedByHearingDay(COURT_CODE, A_DATE);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenSave_thenSaveHearing_Offender_AndDefendant() {
        when(offenderRepositoryFacade.updateOffenderIfItExists(OFFENDER)).thenReturn(OFFENDER);
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(OFFENDER.withProbationStatus(NOT_SENTENCED)));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT.withDefendantName("Charlemagne")));

        facade.save(HEARING);

        verify(offenderRepositoryFacade).updateOffenderIfItExists(any(OffenderEntity.class));
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).saveAll(List.of(OFFENDER));
        verify(defendantRepository).saveAll(List.of(DEFENDANT));
        verify(hearingRepository).save(HEARING);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenSave_andOffenderAndDefendantUnchanged_thenSaveHearing_Only() {
        when(offenderRepositoryFacade.updateOffenderIfItExists(OFFENDER)).thenReturn(OFFENDER);
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT));

        facade.save(HEARING);

        verify(offenderRepositoryFacade).updateOffenderIfItExists(OFFENDER);
        verify(offenderRepository).saveAll(List.of());
        verify(defendantRepository).saveAll(List.of());
        verify(hearingRepository).save(HEARING);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenSaveHearingWithMultipleDefendants_thenSaveHearing_Case_AllOffenders_AndAllDefendants() {
        when(offenderRepositoryFacade.updateOffenderIfItExists(OFFENDER)).thenReturn(OFFENDER.withCrn(CRN));
        when(offenderRepositoryFacade.updateOffenderIfItExists(OFFENDER_2)).thenReturn(OFFENDER_2.withCrn(CRN_2));

        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(OFFENDER.withProbationStatus(NOT_SENTENCED)));
        when(offenderRepository.findByCrn(CRN_2)).thenReturn(Optional.of(OFFENDER_2.withProbationStatus(NOT_SENTENCED)));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT.withDefendantName("Charlemagne")));
        when(defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT.withDefendantName("Charlemagne")));

        facade.save(HEARING_WITH_MULTIPLE_DEFENDANTS);

        verify(offenderRepositoryFacade, times(2)).updateOffenderIfItExists(any(OffenderEntity.class));
        verify(offenderRepositoryFacade).updateOffenderIfItExists(OFFENDER);
        verify(offenderRepositoryFacade).updateOffenderIfItExists(OFFENDER_2);
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).findByCrn(CRN_2);
        verify(offenderRepository).saveAll(List.of(OFFENDER_2, OFFENDER));
        verify(defendantRepository).saveAll(List.of(DEFENDANT_2, DEFENDANT));
        verify(hearingRepository).save(HEARING_WITH_MULTIPLE_DEFENDANTS);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenSaveHearingWithOffenders_thenPassCompleteDataToHearingRepository() {

        final var date = LocalDate.of(2022, 3, 16);
        OffenderEntity updatedOffender = OFFENDER
            .withBreach(true)
            .withAwaitingPsr(true)
            .withProbationStatus(NOT_SENTENCED)
            .withPreviouslyKnownTerminationDate(date)
            .withSuspendedSentenceOrder(true)
            .withPreSentenceActivity(true);

        when(offenderRepositoryFacade.updateOffenderIfItExists(updatedOffender)).thenReturn(updatedOffender.withId(1L));

        final var updatedHearing = HEARING.withHearingDefendants(List.of(HearingDefendantEntity.builder()
                .defendantId(DEFENDANT_ID)
                .defendant(DEFENDANT.withOffender(updatedOffender
                ))
                .build()));
        facade.save(updatedHearing);

        verify(hearingRepository).save(hearingCaptor.capture());
        var savedHearing = hearingCaptor.getValue();

        final var offenderEntity = savedHearing.getHearingDefendants().get(0).getDefendant().getOffender();
        assertThat(offenderEntity.getId()).isEqualTo(1);
        assertThat(offenderEntity.isBreach()).isEqualTo(true);
        assertThat(offenderEntity.getAwaitingPsr()).isEqualTo(true);
        assertThat(offenderEntity.getProbationStatus()).isEqualTo(NOT_SENTENCED);
        assertThat(offenderEntity.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(offenderEntity.isSuspendedSentenceOrder()).isEqualTo(true);
        assertThat(offenderEntity.isPreSentenceActivity()).isEqualTo(true);
    }
}
