package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SexTest {

    @Test
    void whenFemale_thenReturn() {
        assertThat(Sex.fromString("FEMALE")).isSameAs(Sex.FEMALE);
        assertThat(Sex.fromString("F")).isSameAs(Sex.FEMALE);
    }

    @Test
    void whenMale_thenReturn() {
        assertThat(Sex.fromString("MALE")).isSameAs(Sex.MALE);
        assertThat(Sex.fromString("M")).isSameAs(Sex.MALE);
    }

    @Test
    void whenUnexpected_thenReturnNotKnown() {
        assertThat(Sex.fromString(null)).isSameAs(Sex.NOT_KNOWN);
        assertThat(Sex.fromString("     ")).isSameAs(Sex.NOT_KNOWN);
    }

}
