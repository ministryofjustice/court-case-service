package uk.gov.justice.probation.courtcaseservice.controller.model;


import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HearingNoteResponseTest {

    @Test
    void givenHearingNoteEntity_shouldMapToHearingNoteResponse() {
        LocalDateTime now = LocalDateTime.now();
        var hearingNoteEntity = HearingNoteEntity.builder()
            .hearingId("test-hearing-id")
            .author("Author One")
            .note("Note one")
            .id(1234L)
            .createdByUuid("test-uuid")
            .created(now)
            .lastUpdated(now)
            .draft(true)
            .legacy(true)
            .build();

        var hearingNoteResponse = HearingNoteResponse.of(hearingNoteEntity);

        assertThat(hearingNoteResponse).isEqualTo(
            HearingNoteResponse.builder()
                .hearingId("test-hearing-id")
                .author("Author One")
                .note("Note one")
                .noteId(1234L)
                .createdByUuid("test-uuid")
                .created(now)
                .draft(true)
                .legacy(true)
                .build()
        );
    }

    @Test
    void givenHearingNoteEntity_updated_shouldMapToHearingNoteResponse() {
        LocalDateTime createdDateTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedDateTime = LocalDateTime.now();
        var hearingNoteEntity = HearingNoteEntity.builder()
                .hearingId("test-hearing-id")
                .author("Author One")
                .note("Note one")
                .id(1234L)
                .createdByUuid("test-uuid")
                .created(createdDateTime)
                .lastUpdated(updatedDateTime)
                .draft(true)
                .legacy(true)
                .build();

        var hearingNoteResponse = HearingNoteResponse.of(hearingNoteEntity);

        assertThat(hearingNoteResponse).isEqualTo(
                HearingNoteResponse.builder()
                        .hearingId("test-hearing-id")
                        .author("Author One")
                        .note("Note one")
                        .noteId(1234L)
                        .createdByUuid("test-uuid")
                        .created(createdDateTime)
                        .draft(true)
                        .legacy(true)
                        .edited(true)
                        .build()
        );
    }


    @Test
    void givenHearingNoteEntity_not_updated_shouldMapToHearingNoteResponse() {
        LocalDateTime createdDateTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedDateTime = LocalDateTime.now();
        var hearingNoteEntity = HearingNoteEntity.builder()
                .hearingId("test-hearing-id")
                .author("Author One")
                .note("Note one")
                .id(1234L)
                .createdByUuid("test-uuid")
                .created(createdDateTime)
                .draft(true)
                .legacy(true)
                .build();

        var hearingNoteResponse = HearingNoteResponse.of(hearingNoteEntity);

        assertThat(hearingNoteResponse).isEqualTo(
                HearingNoteResponse.builder()
                        .hearingId("test-hearing-id")
                        .author("Author One")
                        .note("Note one")
                        .noteId(1234L)
                        .createdByUuid("test-uuid")
                        .created(createdDateTime)
                        .draft(true)
                        .legacy(true)
                        .edited(false)
                        .build()
        );
    }
}
