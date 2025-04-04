package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class OffenderProbationStatusTest {

    @Test
    void whenNormalStringValues_thenReturnEnumValue() {
        assertThat(OffenderProbationStatus.of("CURRENT")).isSameAs(OffenderProbationStatus.CURRENT);
        assertThat(OffenderProbationStatus.of("NOT_SENTENCED")).isSameAs(OffenderProbationStatus.NOT_SENTENCED);
        assertThat(OffenderProbationStatus.of("PREVIOUSLY_KNOWN")).isSameAs(OffenderProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(OffenderProbationStatus.of("UNCONFIRMED_NO_RECORD")).isSameAs(OffenderProbationStatus.UNCONFIRMED_NO_RECORD);
        assertThat(OffenderProbationStatus.of("CONFIRMED_NO_RECORD")).isSameAs(OffenderProbationStatus.CONFIRMED_NO_RECORD);
    }

    @Test
    void whenReceiveNull_thenReturnNull() {
        assertThat(OffenderProbationStatus.of(null)).isNull();
    }

    @Test
    void whenReceiveUnknownStringValue_thenThrow() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> OffenderProbationStatus.of("XXXXX"));
    }

    @Test
    void whenReceiveNullOrEmptyStringValue_thenThrow() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> OffenderProbationStatus.of("  "));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> OffenderProbationStatus.of(""));
    }

    @Test
    void whenStringValuesWithSpaces_thenReturnEnumValue() {
        assertThat(OffenderProbationStatus.of("current")).isSameAs(OffenderProbationStatus.CURRENT);
        assertThat(OffenderProbationStatus.of("not sentenced")).isSameAs(OffenderProbationStatus.NOT_SENTENCED);
        assertThat(OffenderProbationStatus.of("previously known")).isSameAs(OffenderProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(OffenderProbationStatus.of("unconfirmed no record")).isSameAs(OffenderProbationStatus.UNCONFIRMED_NO_RECORD);
        assertThat(OffenderProbationStatus.of("confirmed no record")).isSameAs(OffenderProbationStatus.CONFIRMED_NO_RECORD);
    }

    @Test
    public void whenCallAsDefendantProbationStatus_thenReturnEquivalent() {
        assertThat(OffenderProbationStatus.CURRENT.asDefendantProbationStatus()).isEqualTo(DefendantProbationStatus.CURRENT);
        assertThat(OffenderProbationStatus.PREVIOUSLY_KNOWN.asDefendantProbationStatus()).isEqualTo(DefendantProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(OffenderProbationStatus.NOT_SENTENCED.asDefendantProbationStatus()).isEqualTo(DefendantProbationStatus.NOT_SENTENCED);
        assertThat(OffenderProbationStatus.UNCONFIRMED_NO_RECORD.asDefendantProbationStatus()).isEqualTo(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
        assertThat(OffenderProbationStatus.CONFIRMED_NO_RECORD.asDefendantProbationStatus()).isEqualTo(DefendantProbationStatus.CONFIRMED_NO_RECORD);
    }
}

