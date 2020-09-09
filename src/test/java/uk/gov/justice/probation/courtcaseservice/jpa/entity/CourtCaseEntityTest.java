package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourtCaseEntityTest {

    private static final LocalDate date = LocalDate.of(2020, Month.JANUARY, 1);

    @Test
    void getSession() {

        CourtCaseEntity caseEntity = CourtCaseEntity.builder()
                            .sessionStartTime(LocalDateTime.of(date, LocalTime.of(11, 59, 59)))
                            .build();
        assertThat(caseEntity.getSession()).isSameAs(CourtSession.MORNING);
    }

    @Test
    void givenNormalDefendantName_whenGetSurname() {
        CourtCaseEntity caseEntity = CourtCaseEntity.builder()
            .defendantName("Mr Nicholas  CAGE")
            .build();
        assertThat(caseEntity.getDefendantSurname()).isEqualTo("CAGE");
    }

    @Test
    void givenNull_whenGetSurname_ThenReturnEmptyString() {
        CourtCaseEntity caseEntity = CourtCaseEntity.builder().build();
        assertThat(caseEntity.getDefendantSurname()).isEqualTo("");
    }

    @Test
    void givenSingleName_whenGetSurname_ThenReturnIt() {
        CourtCaseEntity caseEntity = CourtCaseEntity.builder().defendantName("CAGE").build();
        assertThat(caseEntity.getDefendantSurname()).isEqualTo("CAGE");
    }
}
