package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamePropertiesEntityTest {

    @Test
    void givenNulls_whenGetFullName_thenReturn() {
        final var name = NamePropertiesEntity.builder().title("Mr").surname("JONES").forename1("Bill").build();

        assertThat(name.getFullName()).isEqualTo("Mr Bill JONES");
    }
}
