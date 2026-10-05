package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingNoteResponse {
    private final Long noteId;
    private final String hearingId;
    private final String note;
    private final LocalDateTime created;
    private final String author;
    private final String createdByUuid;
    private final boolean draft;
    private final boolean legacy;
    private final boolean edited;

    public static HearingNoteResponse of(HearingNoteEntity hearingNoteEntity) {
        return HearingNoteResponse.builder()
            .note(hearingNoteEntity.getNote())
            .noteId(hearingNoteEntity.getId())
            .hearingId(hearingNoteEntity.getHearingId())
            .created(hearingNoteEntity.getCreated())
            .author(hearingNoteEntity.getAuthor())
            .createdByUuid(hearingNoteEntity.getCreatedByUuid())
            .draft(hearingNoteEntity.isDraft())
            .legacy(hearingNoteEntity.isLegacy())
            .edited(hearingNoteEntity.getLastUpdated() != null && hearingNoteEntity.getLastUpdated().isAfter(hearingNoteEntity.getCreated()))
            .build();
    }
}
