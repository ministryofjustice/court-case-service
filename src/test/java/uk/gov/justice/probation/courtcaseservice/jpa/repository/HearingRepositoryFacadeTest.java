package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingCourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.service.HearingEntityInitService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.NOT_SENTENCED;

@ExtendWith(MockitoExtension.class)
class HearingRepositoryFacadeTest {

    private static final String COURT_CODE = "courtCode";
    private static final String CASE_NO = "caseNo";
    private static final String LIST_NO = "listNo";
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
            .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
            .hearingDefendants(List.of(HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID)
                    .defendant(DEFENDANT)

                    .build()))
            .build();
    private static final CaseCommentEntity CASE_COMMENT_ONE = CaseCommentEntity.builder().comment("comment one").build();

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
    @Mock
    private CaseCommentsRepository caseCommentsRepository;
    @Mock
    private HearingEntityInitService hearingEntityInitService;
    @Mock
    private HearingCourtCaseRepository hearingCourtCaseRepository;

    @Captor
    private ArgumentCaptor<HearingEntity> hearingCaptor;

    @InjectMocks
    private HearingRepositoryFacade facade;

    @Test
    void whenFindFirstByHearingIdOrderByIdDesc_thenReturnDefendants() {
        when(hearingEntityInitService.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        final var actual = facade.findFirstByHearingId(HEARING_ID);

        verify(hearingEntityInitService).findFirstByHearingId(HEARING_ID);
        verifyNoMoreInteractions(hearingEntityInitService, defendantRepository);
    }

    @Test
    void givenCaseDoesNotExistWithLisNoAndWithNullListNo_whenFindByCourtCodeCaseNoAndListNo_thenFallbackToMostRecentCase_andReturnHearingIdSetToNull() {
        when(hearingEntityInitService.findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, LIST_NO)).thenReturn(Optional.empty());
        when(hearingEntityInitService.findMostRecentByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));

        final var actual = facade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, LIST_NO);
        Assertions.assertThat(actual.get().getHearingId()).isNull();

        AssertionsForClassTypes.assertThat(actual).get().isEqualTo(HEARING_WITH_MULTIPLE_DEFENDANTS);
        verify(hearingEntityInitService).findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, LIST_NO);
        verify(hearingEntityInitService).findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, null);
        verify(hearingEntityInitService).findMostRecentByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);
        verifyNoMoreInteractions(hearingEntityInitService, defendantRepository);
    }

    @Test
    void givenCaseDoesNotExistWithCourtCodeAndCaseNo_withOrWithoutLisNo_whenFindByCourtCodeCaseNoAndListNo_thenReturnEmpty() {
        when(hearingEntityInitService.findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, LIST_NO)).thenReturn(Optional.empty());
        when(hearingEntityInitService.findMostRecentByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());

        var actual = facade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, LIST_NO);

        AssertionsForClassTypes.assertThat(actual).isEmpty();

        verify(hearingEntityInitService).findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, LIST_NO);
        verify(hearingEntityInitService).findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, null);
        verify(hearingEntityInitService).findMostRecentByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);
        verifyNoInteractions(defendantRepository);
        verifyNoMoreInteractions(hearingEntityInitService);
    }

    @Test
    void whenFindByCourtCodeCaseNoAnd_NoListNoProvided_thenReturnDefendants() {
        when(hearingEntityInitService.findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, null)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));
        final var actual = facade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, null);
        Assertions.assertThat(actual.get().getHearingId()).isEqualTo(HEARING_ID);

        AssertionsForClassTypes.assertThat(actual).get().isEqualTo(HEARING_WITH_MULTIPLE_DEFENDANTS);
        verify(hearingEntityInitService).findByCourtCodeCaseNoAndListNo(COURT_CODE, CASE_NO, null);
        verifyNoMoreInteractions(hearingEntityInitService, defendantRepository);
    }

    @Test
    void whenFindByHearingIdAndDefendantId_thenReturnAHearingWithDefendantAndCaseComments() {
        when(hearingEntityInitService.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));
        when(caseCommentsRepository.findByCaseIdAndDefendantIdAndDeletedFalse(COURT_CASE.getCaseId(), DEFENDANT_ID)).thenReturn(List.of(CASE_COMMENT_ONE));

        final var actual = facade.findByHearingIdAndDefendantId(HEARING_ID, DEFENDANT_ID);

        verify(hearingEntityInitService).findFirstByHearingId(HEARING_ID);
        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndDeletedFalse(COURT_CASE.getCaseId(), DEFENDANT_ID);

        HearingEntity hearing = actual.get();
        AssertionsForClassTypes.assertThat(hearing).isEqualTo(HEARING);
        AssertionsForClassTypes.assertThat(hearing.getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
        AssertionsForClassTypes.assertThat(hearing.getCourtCase()).isEqualTo(COURT_CASE.withCaseComments(List.of(CASE_COMMENT_ONE)));
    }

    @Test
    void givenMultipleDefendants_whenFindByHearingIdAndDefendantId_thenReturnAHearingWithAllDefendants() {
        when(hearingEntityInitService.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING_WITH_MULTIPLE_DEFENDANTS));

        final var actual = facade.findByHearingIdAndDefendantId(HEARING_ID, DEFENDANT_ID);
        AssertionsForClassTypes.assertThat(actual).get().isEqualTo(HEARING);
        AssertionsForClassTypes.assertThat(actual.get().getHearingDefendant(DEFENDANT_ID).getDefendant()).isEqualTo(DEFENDANT);
        AssertionsForClassTypes.assertThat(actual.get().getHearingDefendant(DEFENDANT_ID_2).getDefendant()).isEqualTo(DEFENDANT_2);
    }

    @Test
    void givenDefendantIdNotOnCase_whenFindByHearingIdAndDefendantId_thenReturnEmpty() {
        when(hearingEntityInitService.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(HEARING));

        AssertionsForClassTypes.assertThat(facade.findByHearingIdAndDefendantId(HEARING_ID, "THE_WRONG_DEFENDANT_ID")).isEmpty();
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_andDateTimeIsMinMax_thenCallRepoMethodWithoutDateConstraints() {
        when(hearingEntityInitService.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, LocalDateTime.MIN, LocalDateTime.MAX);
        AssertionsForClassTypes.assertThat(actual.size()).isEqualTo(2);
        AssertionsForClassTypes.assertThat(actual.get(0).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT);
        AssertionsForClassTypes.assertThat(actual.get(1).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT_2);
        AssertionsForClassTypes.assertThat(actual.get(1).getHearingDefendants().get(1).getDefendant()).isEqualTo(DEFENDANT);

        verify(hearingEntityInitService).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_andDateTimesAreNull_thenCallRepoMethodWithoutDateConstraints() {
        when(hearingEntityInitService.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, null, null);
        AssertionsForClassTypes.assertThat(actual.size()).isEqualTo(2);
        AssertionsForClassTypes.assertThat(actual.get(0).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT);
        AssertionsForClassTypes.assertThat(actual.get(1).getHearingDefendants().get(0).getDefendant()).isEqualTo(DEFENDANT_2);
        AssertionsForClassTypes.assertThat(actual.get(1).getHearingDefendants().get(1).getDefendant()).isEqualTo(DEFENDANT);

        verify(hearingEntityInitService).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenFindByCourtCodeAndHearingDay_thenReturnDefendants() {
        when(hearingEntityInitService.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME))
                .thenReturn(List.of(HEARING, HEARING_WITH_MULTIPLE_DEFENDANTS));

        final var actual = facade.findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME);

        verify(hearingEntityInitService).findByCourtCodeAndHearingDay(COURT_CODE, A_DATE, A_DATETIME, A_DATETIME);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void findLastModifiedByHearingDay() {
        when(hearingRepository.findLastModifiedByHearingDay(COURT_CODE, A_DATE)).thenReturn(Optional.of(A_DATETIME));
        final var actual = facade.findLastModifiedByHearingDay(COURT_CODE, A_DATE);
        AssertionsForClassTypes.assertThat(actual).get().isEqualTo(A_DATETIME);
        verify(hearingRepository).findLastModifiedByHearingDay(COURT_CODE, A_DATE);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    void whenSave_thenSaveHearing_Offender_AndDefendant() {
        when(offenderRepositoryFacade.upsertOffender(OFFENDER)).thenReturn(OFFENDER);
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(HearingEntity.builder().build());
        facade.save(HEARING);

        verify(offenderRepositoryFacade).upsertOffender(any(OffenderEntity.class));
        verify(defendantRepository).findFirstByDefendantId(HEARING.getHearingDefendants().get(0).getDefendantId());
        verify(hearingRepository).save(HEARING);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }

    @Test
    @Disabled("Is this a possible case?")
    void givenMultipleDefendantsWitSameOffender_whenSave_thenSaveHearing_Offender_AndDefendant() {
        when(offenderRepositoryFacade.updateOffenderIfItExists(OFFENDER)).thenReturn(OFFENDER);
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());
        when(defendantRepository.findFirstByDefendantId(DEFENDANT_ID)).thenReturn(Optional.empty());
        when(defendantRepository.findFirstByDefendantId(DEFENDANT_ID_2)).thenReturn(Optional.empty());
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(HearingEntity.builder().build());

        DefendantEntity DEFENDANT_2 = DEFENDANT.withDefendantId(DEFENDANT_ID_2);
        facade.save(HEARING.withHearingDefendants(
                List.of(HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID)
                    .defendant(DEFENDANT)
                    .build(),
                    HearingDefendantEntity.builder()
                    .defendantId(DEFENDANT_ID_2)
                    .defendant(DEFENDANT_2)
                    .build())
            )
        );

        verify(offenderRepositoryFacade, times(2)).updateOffenderIfItExists(any(OffenderEntity.class));
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).saveAll(List.of(OFFENDER));
        verify(defendantRepository).saveAll(List.of(DEFENDANT, DEFENDANT_2));
        verify(hearingRepository).save(HEARING);
        verifyNoMoreInteractions(hearingRepository, defendantRepository, offenderRepository);
    }

    @Test
    void whenSaveHearingWithMultipleDefendants_thenSaveHearing_Case_AllOffenders_AndAllDefendants() {
        when(offenderRepositoryFacade.upsertOffender(OFFENDER)).thenReturn(OFFENDER.withCrn(CRN));
        when(offenderRepositoryFacade.upsertOffender(OFFENDER_2)).thenReturn(OFFENDER_2.withCrn(CRN_2));

        when(defendantRepository.findFirstByDefendantId(DEFENDANT_ID)).thenReturn(Optional.of(DEFENDANT.withDefendantName("Charlemagne")));
        when(defendantRepository.findFirstByDefendantId(DEFENDANT_ID_2)).thenReturn(Optional.of(DEFENDANT.withDefendantName("Charlemagne")));
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(HearingEntity.builder().build());

        facade.save(HEARING_WITH_MULTIPLE_DEFENDANTS);

        verify(offenderRepositoryFacade, times(2)).upsertOffender(any(OffenderEntity.class));
        verify(offenderRepositoryFacade).upsertOffender(OFFENDER);
        verify(offenderRepositoryFacade).upsertOffender(OFFENDER_2);
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

        when(offenderRepositoryFacade.upsertOffender(updatedOffender)).thenReturn(updatedOffender.withId(1L));
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(HearingEntity.builder().build());

        final var updatedHearing = HEARING.withHearingDefendants(List.of(HearingDefendantEntity.builder()
                .defendantId(DEFENDANT_ID)
                .defendant(DEFENDANT.withOffender(updatedOffender
                ))
                .build()));
        facade.save(updatedHearing);

        verify(hearingRepository).save(hearingCaptor.capture());
        var savedHearing = hearingCaptor.getValue();

        final var offenderEntity = savedHearing.getHearingDefendants().get(0).getDefendant().getOffender();
        AssertionsForClassTypes.assertThat(offenderEntity.getId()).isEqualTo(1);
        AssertionsForClassTypes.assertThat(offenderEntity.isBreach()).isEqualTo(true);
        AssertionsForClassTypes.assertThat(offenderEntity.getAwaitingPsr()).isEqualTo(true);
        AssertionsForClassTypes.assertThat(offenderEntity.getProbationStatus()).isEqualTo(NOT_SENTENCED);
        AssertionsForClassTypes.assertThat(offenderEntity.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        AssertionsForClassTypes.assertThat(offenderEntity.isSuspendedSentenceOrder()).isEqualTo(true);
        AssertionsForClassTypes.assertThat(offenderEntity.isPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    void givenDefendantAlreadyExist_saveIncomingHearingWithSameDefendant_thenUpdateExistingDefendantAndMerge() {
        when(offenderRepositoryFacade.upsertOffender(OFFENDER)).thenReturn(OFFENDER);
        var existingDefendant = DEFENDANT.withId(10L).withDefendantName("Mr. Existing Name");
        when(defendantRepository.findFirstByDefendantId(DEFENDANT_ID)).thenReturn(Optional.ofNullable(existingDefendant));
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(HearingEntity.builder().build());
        facade.save(HEARING);

        verify(offenderRepositoryFacade).upsertOffender(any(OffenderEntity.class));
        HearingDefendantEntity expectedHearingDefendant = HEARING.getHearingDefendants().get(0);
        verify(defendantRepository).findFirstByDefendantId(expectedHearingDefendant.getDefendantId());
        var expectedDefendant = existingDefendant.withDefendantName(DEFENDANT.getDefendantName());
        expectedHearingDefendant.setDefendant(expectedDefendant);
        var expectedHearing = HEARING.withHearingDefendants(List.of(expectedHearingDefendant));
        verify(hearingRepository).save(HEARING);
        verifyNoMoreInteractions(hearingRepository, defendantRepository);
    }
}
