package uk.gov.justice.probation.courtcaseservice.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID2;

@ExtendWith(MockitoExtension.class)
class CaseProgressServiceTest {

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private HearingNotesRepository hearingNotesRepository;

    @InjectMocks
    private CaseProgressService caseProgressService;

    private static final String CASE_ID = "test-case-id";

    @Test
    void givenCaseId_shouldFetchAllHearingsUsingHearingRepo_thenMapToCaseProgressHearing() {
        LocalTime timeNow = LocalTime.of(10, 10, 10);
        LocalDate dateNow = LocalDate.of(2022, 9, 22);
        var courtCase = CourtCaseEntity.builder().sourceType(SourceType.COMMON_PLATFORM).build();
        var courtName = "Sheffield mags";
        var court = CourtEntity.builder().name(courtName).build();
        var createdDateTime = LocalDateTime.now();
        String hearingIdOne = "hearing-id-one";

        List<HearingNoteEntity> hearing1DefendantOneNotes = List.of(HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id one defendant 1 note one").created(createdDateTime).build(),
            HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id one defendant 1 note two").created(createdDateTime).build());

        List<HearingNoteEntity> hearing1DefendantTwoNotes = List.of(HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id defendant 2 note one").created(createdDateTime).build(),
            HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id one defendant 2 note two").created(createdDateTime).build());

        List<HearingDayEntity> hearingDays = List.of(HearingDayEntity.builder().court(court).day(dateNow).time(timeNow).created(createdDateTime).build());

        HearingDefendantEntity hearing1HearingDefendant1 = HearingDefendantEntity.builder().notes(new ArrayList<>()).defendantId(DEFENDANT_ID).build();
        hearing1HearingDefendant1.getNotes().addAll(hearing1DefendantOneNotes);

        HearingDefendantEntity hearing1HearingDefendant2 = HearingDefendantEntity.builder().notes(new ArrayList<>()).defendantId(DEFENDANT_ID2).build();
        hearing1HearingDefendant2.getNotes().addAll(hearing1DefendantTwoNotes);

        HearingEntity hearing1 = HearingEntity.builder().hearingId(hearingIdOne).courtCase(courtCase).hearingDays(hearingDays)
            .hearingDefendants(List.of(hearing1HearingDefendant1, hearing1HearingDefendant2))
            .build();

        HearingEntity hearing2 = HearingEntity.builder().hearingId("hearing-id-two").courtCase(courtCase).hearingDays(hearingDays)
            .hearingDefendants(List.of(
                HearingDefendantEntity.builder().defendantId(DEFENDANT_ID).notes(new ArrayList<>()).build(),
                HearingDefendantEntity.builder().defendantId(DEFENDANT_ID2).notes(new ArrayList<>()).build()
                ))
            .build();

        List<HearingEntity> hearingEntities = List.of(hearing1,
            hearing2);

        given(hearingRepository.findHearingsByCaseId(CASE_ID)).willReturn(
            Optional.of(hearingEntities));

        List<CaseProgressHearing> expected = List.of(
            CaseProgressHearing.builder().hearingId("hearing-id-one").hearingDateTime(LocalDateTime.of(dateNow, timeNow)).court(courtName).session(MORNING.name())
                .hearingTypeLabel("Hearing type unknown")
                .notes(List.of(
                    HearingNoteResponse.builder().hearingId(hearingIdOne).note("Hearing id one defendant 1 note one").created(createdDateTime).build(),
                    HearingNoteResponse.builder().hearingId(hearingIdOne).note("Hearing id one defendant 1 note two").created(createdDateTime).build())
                )
                .build(),
            CaseProgressHearing.builder().hearingId("hearing-id-two").hearingDateTime(LocalDateTime.of(dateNow, timeNow)).court(courtName).session(MORNING.name())
                .notes(new ArrayList<>())
                .hearingTypeLabel("Hearing type unknown").build());

        var progress = caseProgressService.getCaseHearingProgress(CASE_ID, DEFENDANT_ID);

        verify(hearingRepository).findHearingsByCaseId(CASE_ID);
        Assertions.assertThat(progress).isEqualTo(expected);
    }
}
