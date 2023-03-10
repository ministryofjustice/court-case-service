package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HearingNoteEntityTest {

    @Test
    void shouldUpdateNote() {
        var hearingNote = HearingNoteEntity.builder().note("Old note")
            .hearingId("test-id")
            .id(1L)
            .author("auth one")
            .createdByUuid("uuid")
            .draft(false)
            .build();

        var updatedNote = HearingNoteEntity.builder().note("updated note").draft(true).build();
        hearingNote.updateNote(updatedNote);
        assertThat(hearingNote).isEqualTo(hearingNote.withNote("updated note").withDraft(true));
    }
}