package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProbationStatusTest {

    @Test
    void whenNormalStringValues_thenReturnEnumValue() {
        assertThat(ProbationStatus.of("CURRENT")).isSameAs(ProbationStatus.CURRENT);
        assertThat(ProbationStatus.of("NOT_SENTENCED")).isSameAs(ProbationStatus.NOT_SENTENCED);
        assertThat(ProbationStatus.of("PREVIOUSLY_KNOWN")).isSameAs(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(ProbationStatus.of("NO_RECORD")).isSameAs(ProbationStatus.NO_RECORD);
    }

    @Test
    void whenReceiveUnknownStringValue_thenReturnDefaultEnumValue() {
        assertThat(ProbationStatus.of("XXXXX")).isSameAs(ProbationStatus.NO_RECORD);
    }

    @Test
    void whenReceiveNullOrEmptyStringValue_thenReturnDefaultEnumValue() {
        assertThat(ProbationStatus.of("  ")).isSameAs(ProbationStatus.NO_RECORD);
        assertThat(ProbationStatus.of("")).isSameAs(ProbationStatus.NO_RECORD);
        assertThat(ProbationStatus.of(null)).isSameAs(ProbationStatus.NO_RECORD);
    }
}
