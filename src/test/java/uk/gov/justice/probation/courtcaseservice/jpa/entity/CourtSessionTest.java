package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.AFTERNOON;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING;

class CourtSessionTest {
    @Test
    void fromShouldReturnMorningBefore12() {
        assertThat(CourtSession.from(LocalDateTime.of(2020, 1, 1, 0, 0, 0)))
                .isEqualTo(MORNING);
        assertThat(CourtSession.from(LocalDateTime.of(2020, 1, 1, 11, 59, 59)))
                .isEqualTo(MORNING);
    }

    @Test
    void fromShouldReturnAfternoonAfter12() {
        assertThat(CourtSession.from(LocalDateTime.of(2020, 1, 1, 12, 0, 0)))
                .isEqualTo(AFTERNOON);
        assertThat(CourtSession.from(LocalDateTime.of(2020, 1, 1, 23, 59, 59)))
                .isEqualTo(AFTERNOON);
    }
}
