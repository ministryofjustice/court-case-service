package uk.gov.justice.probation.courtcaseservice.service.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@Schema(description = "CaseProgressItem")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@With
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseProgressHearing {
    private final String hearingId;
    private final String court;
    private final String courtRoom;
    private final String session;
    private final String hearingTypeLabel;
    private final LocalDateTime hearingDateTime;

    private final List<HearingNoteResponse> notes;

    public static CaseProgressHearing of(HearingEntity hearingEntity, Optional<List<HearingNoteEntity>> notes) {
        var hearingDay = getHearingDay(hearingEntity);
        return CaseProgressHearing.builder()
            .hearingId(hearingEntity.getHearingId())
            .hearingDateTime(hearingDay.getSessionStartTime())
            .session(hearingDay.getSession().name())
            .hearingTypeLabel(getHearingTypeLabel(hearingEntity))
            .court(hearingDay.getCourt().getName())
            .courtRoom(hearingDay.getCourtRoom())
            .notes(notes.map(hearingNoteEntities -> hearingNoteEntities.stream().map(HearingNoteResponse::of).collect(Collectors.toList())).orElse(null))
            .build();
    }

    // It was decided to go with the first hearing day info and revisit when multi day hearing analysis is completed
    private static HearingDayEntity getHearingDay(HearingEntity hearingEntity) {
        return hearingEntity.getHearingDays().stream().min(Comparator.comparing(HearingDayEntity::getSessionStartTime)).get();
    }

    private static String getHearingTypeLabel(HearingEntity hearingEntity) {
        return hearingEntity.getSourceType() == COMMON_PLATFORM ? hearingEntity.getHearingType() : String.format("%s hearing", hearingEntity.getListNo());
    }
}
