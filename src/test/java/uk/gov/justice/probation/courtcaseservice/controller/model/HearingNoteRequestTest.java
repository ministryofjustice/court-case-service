package uk.gov.justice.probation.courtcaseservice.controller.model;


import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import static org.assertj.core.api.Assertions.assertThat;

class HearingNoteRequestTest {

    @Test
    void givenHearingNoteRequest_shouldMapToHearingNoteEntity() {

        var hearingNoteRequest = HearingNoteRequest.builder()
            .hearingId("test-hearing-id")
            .author("Author One")
            .note("Note one")
            .build();

        var hearingNoteEntity = hearingNoteRequest.asEntity("test-uuid");

        assertThat(hearingNoteEntity).isEqualTo(
            HearingNoteEntity.builder()
                .hearingId("test-hearing-id")
                .author("Author One")
                .note("Note one")
                .createdByUuid("test-uuid")
                .build()
        );
    }

}