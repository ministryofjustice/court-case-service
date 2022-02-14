package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HearingDayEntityTest {

    @Test
    public void testLoggableString() {
        final var hearing = HearingDayEntity.builder()
                .day(LocalDate.of(2021, 11, 1))
                .time(LocalTime.of(9, 30))
                .courtRoom("Court room 1")
                .courtCode("B10JQ")
                .listNo("1st")
                .build();

        assertThat(hearing.loggableString()).isEqualTo("B10JQ|Court room 1|2021-11-01T09:30");
    }
}
