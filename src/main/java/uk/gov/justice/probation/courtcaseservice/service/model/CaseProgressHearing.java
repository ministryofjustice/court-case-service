package uk.gov.justice.probation.courtcaseservice.service.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDateTime;
import java.util.Comparator;

import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@Schema(description = "CaseProgressItem")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseProgressHearing {
    private final String hearingId;
    private final String court;
    private final String courtRoom;
    private final String session;
    private final String hearingTypeLabel;
    private final LocalDateTime hearingDateTime;

    public static CaseProgressHearing of(HearingEntity hearingEntity) {
        var hearingDay = getHearingDay(hearingEntity);
        return CaseProgressHearing.builder()
            .hearingId(hearingEntity.getHearingId())
            .hearingDateTime(hearingDay.getSessionStartTime())
            .session(hearingDay.getSession().name())
            .hearingTypeLabel(getHearingTypeLabel(hearingEntity, hearingDay))
            .court(hearingDay.getCourt().getName())
            .courtRoom(hearingDay.getCourtRoom())
            .build();
    }

    // It was decided to go with the first hearing day info and revisit when multi day hearing analysis is completed
    private static HearingDayEntity getHearingDay(HearingEntity hearingEntity) {
        return hearingEntity.getHearingDays().stream().min(Comparator.comparing(HearingDayEntity::getSessionStartTime)).get();
    }

    private static String getHearingTypeLabel(HearingEntity hearingEntity, HearingDayEntity hearingDay) {
        return hearingEntity.getSourceType() == COMMON_PLATFORM ? hearingEntity.getHearingType() : String.format("%s hearing", hearingDay.getListNo());
    }
}
