package uk.gov.justice.probation.courtcaseservice.service.model;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA;

class CaseProgressHearingTest {

    @Test
    void givenCPCase_shouldMapToCaseProgressHearingExcludingDeletedNotes() {
        var hearingDayEntity1 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0)).withCourtRoom("Room 1").withCourt(CourtEntity.builder().name("Leeds mags court").build());
        var hearingDayEntity2 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 5, 5, 9, 0)).withCourtRoom("Room 2").withCourt(CourtEntity.builder().name("Sheffield mags court").build());

        var hearingEntity = EntityHelper.aHearingEntity().withHearingDays(List.of(hearingDayEntity1, hearingDayEntity2)).withHearingType("Sentence");

        var hearingNotes = List.of(
                HearingNoteEntity.builder().note("Note one").build(),
                HearingNoteEntity.builder().note("Note two").build(),
                HearingNoteEntity.builder().note("This delete note should note be mapped in response").deleted(true).build()
        );

        Assertions.assertThat(CaseProgressHearing.of(hearingEntity, DEFENDANT_ID, hearingNotes)).isEqualTo(
            CaseProgressHearing.builder().
            hearingDateTime(LocalDateTime.of(2022, 2, 26, 9, 0))
                .court("Leeds mags court")
                .courtRoom("Room 1")
                .hearingTypeLabel("Sentence")
                .session(MORNING.name())
                .hearingId(HEARING_ID)
                .notes(
                    List.of(HearingNoteResponse.builder().note("Note one").build(), HearingNoteResponse.builder().note("Note two").build())
                )
            .build()
        );
    }

    @Test
    void givenLibraCase_shouldMapToCaseProgressHearing() {
        var hearingDayEntity1 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0))
                                                    .withCourtRoom("Room 1").withCourt(CourtEntity.builder().name("Leeds mags court").build());
        var hearingDayEntity2 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 5, 5, 9, 0))
                                                    .withCourtRoom("Room 2").withCourt(CourtEntity.builder().name("Sheffield mags court").build());

        var hearingEntity = HearingEntity.builder()
            .hearingId("test-hearing-id")
            .hearingType("Sentence")
            .listNo("1st")
            .courtCase(CourtCaseEntity.builder().sourceType(LIBRA).build())
            .hearingDays(List.of(hearingDayEntity2, hearingDayEntity1))
            .hearingDefendants(List.of(EntityHelper.aHearingDefendantEntity()))
            .build();

        var hearingNotes = List.of(
                HearingNoteEntity.builder().note("Note one").build(),
                HearingNoteEntity.builder().note("Note two").build()
            );

        Assertions.assertThat(CaseProgressHearing.of(hearingEntity, DEFENDANT_ID, hearingNotes)).isEqualTo(
            CaseProgressHearing.builder().
            hearingDateTime(LocalDateTime.of(2022, 2, 26, 9, 0))
                .court("Leeds mags court")
                .courtRoom("Room 1")
                .hearingTypeLabel("1st hearing")
                .session(MORNING.name())
                .hearingId("test-hearing-id")
                .notes(
                    List.of(HearingNoteResponse.builder().note("Note one").build(), HearingNoteResponse.builder().note("Note two").build())
                )
            .build()
        );
    }

    @Test
    void givenLibraCase_shouldMapEmptyHearingTypeToDefaultText() {
        var hearingDayEntity1 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0))
                                                    .withCourtRoom("Room 1").withCourt(CourtEntity.builder().name("Leeds Mags").name("Leeds mags court").build());

        var hearingEntity = EntityHelper.aHearingEntity().withHearingType(null)
            .withHearingDays(List.of(hearingDayEntity1));

        CaseProgressHearing caseProgressHearing = CaseProgressHearing.of(hearingEntity, DEFENDANT_ID, Collections.emptyList());
        Assertions.assertThat(caseProgressHearing.getHearingTypeLabel()).isEqualTo("Hearing type unknown");
    }

    @Test
    void givenCommonPlatformCase_shouldMapEmptyHearingTypeToDefaultText() {
        var hearingEntity = EntityHelper.aHearingEntity().withHearingType(null).withHearingDays(
            List.of(EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0))
                .withCourtRoom("Room 1").withCourt(CourtEntity.builder().name("Leeds Mags").name("Leeds mags court").build())));

        CaseProgressHearing caseProgressHearing = CaseProgressHearing.of(hearingEntity, DEFENDANT_ID, Collections.emptyList());
        Assertions.assertThat(caseProgressHearing.getHearingTypeLabel()).isEqualTo("Hearing type unknown");
    }

}