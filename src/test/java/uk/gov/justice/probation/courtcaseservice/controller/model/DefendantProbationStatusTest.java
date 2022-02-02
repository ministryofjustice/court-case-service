package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DefendantProbationStatusTest {

    @Test
    void whenNormalStringValues_thenReturnEnumValue() {
        assertThat(DefendantProbationStatus.of("CURRENT")).isSameAs(DefendantProbationStatus.CURRENT);
        assertThat(DefendantProbationStatus.of("NOT_SENTENCED")).isSameAs(DefendantProbationStatus.NOT_SENTENCED);
        assertThat(DefendantProbationStatus.of("PREVIOUSLY_KNOWN")).isSameAs(DefendantProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(DefendantProbationStatus.of("NO_RECORD")).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }

    @Test
    void whenReceiveUnknownStringValue_thenReturnDefaultEnumValue() {
        assertThat(DefendantProbationStatus.of("XXXXX")).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }

    @Test
    void whenReceiveNullOrEmptyStringValue_thenReturnDefaultEnumValue() {
        assertThat(DefendantProbationStatus.of("  ")).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
        assertThat(DefendantProbationStatus.of("")).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
        assertThat(DefendantProbationStatus.of(null)).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }

    @Test
    void whenStringValuesWithSpaces_thenReturnEnumValue() {
        assertThat(DefendantProbationStatus.of("current")).isSameAs(DefendantProbationStatus.CURRENT);
        assertThat(DefendantProbationStatus.of("not sentenced")).isSameAs(DefendantProbationStatus.NOT_SENTENCED);
        assertThat(DefendantProbationStatus.of("previously known")).isSameAs(DefendantProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(DefendantProbationStatus.of("No Record")).isSameAs(DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }
}
