package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.Test;

class CourtCaseEntityTest {

    private static final LocalDate date = LocalDate.of(2020, Month.JANUARY, 1);

    @Test
    void getSession() {

        CourtCaseEntity caseEntity = CourtCaseEntity.builder()
                            .sessionStartTime(LocalDateTime.of(date, LocalTime.of(11, 59, 59)))
                            .build();
        assertThat(caseEntity.getSession()).isSameAs(CourtSession.MORNING);
    }

}
