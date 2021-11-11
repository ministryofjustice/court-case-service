package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamePropertiesEntityTest {

    @Test
    void givenNulls_whenGetFullName_thenReturn() {
        final var name = NamePropertiesEntity.builder().title("Mr").surname("JONES").forename1("Bill").build();

        assertThat(name.getFullName()).isEqualTo("Mr Bill JONES");
    }

    @Test
    void givenOtherName_whenCompareTo_thenReturnCorrectOrder() {
        var reference = NamePropertiesEntity.builder().title("Mr").surname("POLLARD").build();
        var lesser = NamePropertiesEntity.builder().title("Mr").surname("JONES").build();
        var greater = NamePropertiesEntity.builder().title("Mr").surname("XENOMORPH").build();
        var equal = NamePropertiesEntity.builder().title("Mr").surname("POLLARD").build();

        assertThat(reference.compareTo(lesser)).isPositive();
        assertThat(reference.compareTo(greater)).isNegative();
        assertThat(reference.compareTo(equal)).isEqualTo(0);
    }
}
