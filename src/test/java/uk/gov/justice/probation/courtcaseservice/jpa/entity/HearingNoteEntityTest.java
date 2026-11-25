package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HearingNoteEntityTest {

    @Test
    void shouldUpdateNote() {
        var hearingNote = HearingNoteEntity.builder().note("Old note")
            .hearingId("test-id")
            .id(1L)
            .author("auth one")
            .createdByUuid("UUID")
            .draft(false)
            .deleted(true)
            .build();

        var updatedNote = HearingNoteEntity.builder().note("updated note").draft(true).deleted(false).build();
        hearingNote.updateNote(updatedNote);
        assertThat(hearingNote).isEqualTo(hearingNote.withNote("updated note").withDraft(true));
    }

    @Test
    void shouldResetCreatedWhenDraftIsPublished() {
        var originalCreated = LocalDateTime.now().minusDays(3);
        var hearingNote = HearingNoteEntity.builder().note("Draft note")
            .hearingId("test-id")
            .id(1L)
            .author("auth one")
            .createdByUuid("UUID")
            .created(originalCreated)
            .draft(true)
            .deleted(false)
            .build();

        var publishedNote = HearingNoteEntity.builder().note("Final note").draft(false).deleted(false).build();
        hearingNote.updateNote(publishedNote);

        assertThat(hearingNote.isDraft()).isFalse();
        assertThat(hearingNote.getCreated()).isAfter(originalCreated);
    }

    @Test
    void shouldNotResetCreatedWhenEditingAnAlreadyPublishedNote() {
        var originalCreated = LocalDateTime.now().minusDays(3);
        var hearingNote = HearingNoteEntity.builder().note("Final note")
            .hearingId("test-id")
            .id(1L)
            .author("auth one")
            .createdByUuid("UUID")
            .created(originalCreated)
            .draft(false)
            .deleted(false)
            .build();

        var editedNote = HearingNoteEntity.builder().note("Edited note").draft(false).deleted(false).build();
        hearingNote.updateNote(editedNote);

        assertThat(hearingNote.getCreated()).isEqualTo(originalCreated);
    }
}