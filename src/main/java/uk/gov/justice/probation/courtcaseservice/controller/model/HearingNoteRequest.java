package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingNoteRequest {
    @NotBlank
    private final String hearingId;
    @NotBlank
    private final String note;
    @NotBlank
    private final String author;

    public HearingNoteEntity asEntity(String userUuid) {
        return HearingNoteEntity.builder()
            .note(note)
            .hearingId(hearingId)
            .createdByUuid(userUuid)
            .author(author)
            .build();
    }
}
