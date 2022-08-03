package uk.gov.justice.probation.courtcaseservice.controller.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class HearingDay {
    @NotBlank
    private final String courtCode;
    @NotBlank
    private final String courtRoom;
    @NotNull
    private final LocalDateTime sessionStartTime;
    private final String listNo;

    public static HearingDay of(HearingDayEntity hearingDayEntity) {
        return HearingDay.builder()
            .courtCode(hearingDayEntity.getCourtCode())
            .courtRoom(hearingDayEntity.getCourtRoom())
            .sessionStartTime(Optional.ofNullable(hearingDayEntity.getDay())
                .map(day -> LocalDateTime.of(day, Optional.ofNullable(hearingDayEntity.getTime()).orElse(LocalTime.MIDNIGHT)))
                .orElse(null))
            .listNo(hearingDayEntity.getListNo())
            .build();
    }
}
