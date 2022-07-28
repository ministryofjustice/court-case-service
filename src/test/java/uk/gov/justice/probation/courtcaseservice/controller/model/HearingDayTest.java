package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class HearingDayTest {

    @Test
    void shouldMapToHearingDay() {
        HearingDayEntity hearingDayEntity = HearingDayEntity.builder()
            .day(LocalDate.of(2022, 07, 25))
            .time(LocalTime.of(11, 45))
            .courtRoom("court-room-1")
            .courtCode("court-code")
            .build();
        Assertions.assertThat(HearingDay.of(hearingDayEntity)).isEqualTo(
            HearingDay.builder()
                .courtCode(hearingDayEntity.getCourtCode())
                .courtRoom(hearingDayEntity.getCourtRoom())
                .listNo(hearingDayEntity.getListNo())
                .sessionStartTime(LocalDateTime.of(2022, 07, 25, 11, 45))
                .build());
    }
}