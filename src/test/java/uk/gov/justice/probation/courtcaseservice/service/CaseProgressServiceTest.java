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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING;

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
        String hearingIdOne = "hearing-id-one";

        List<HearingDayEntity> hearingDays = List.of(HearingDayEntity.builder().court(court).day(dateNow).time(timeNow).build());
        List<HearingEntity> hearingEntities = List.of(HearingEntity.builder().hearingId(hearingIdOne).courtCase(courtCase).hearingDays(hearingDays).build(),
            HearingEntity.builder().hearingId("hearing-id-two").courtCase(courtCase).hearingDays(hearingDays).build());

        given(hearingRepository.findHearingsByCaseId(CASE_ID)).willReturn(
            Optional.of(hearingEntities));

        List<HearingNoteEntity> hearingIdOneNotes = List.of(HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id one note one").build(),
            HearingNoteEntity.builder().hearingId(hearingIdOne).note("Hearing id one note two").build());
        given(hearingNotesRepository.findAllByHearingIdAndDeletedFalse(hearingIdOne)).willReturn(
            Optional.of(hearingIdOneNotes
            ));

        List<CaseProgressHearing> expected = List.of(
            CaseProgressHearing.builder().hearingId("hearing-id-one").hearingDateTime(LocalDateTime.of(dateNow, timeNow)).court(courtName).session(MORNING.name())
                .notes(List.of(
                    HearingNoteResponse.builder().hearingId(hearingIdOne).note("Hearing id one note one").build(),
                    HearingNoteResponse.builder().hearingId(hearingIdOne).note("Hearing id one note two").build())
                )
                .build(),
            CaseProgressHearing.builder().hearingId("hearing-id-two").hearingDateTime(LocalDateTime.of(dateNow, timeNow)).court(courtName).session(MORNING.name()).build());

        var progress = caseProgressService.getCaseHearingProgress(CASE_ID);

        verify(hearingRepository).findHearingsByCaseId(CASE_ID);
        Assertions.assertThat(progress).isEqualTo(expected);
    }
}