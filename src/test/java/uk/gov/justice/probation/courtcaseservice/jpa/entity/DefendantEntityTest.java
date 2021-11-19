package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class DefendantEntityTest {

    @Test
    void givenStandard_whenGetSurname_thenReturn() {
        var defendantEntity = DefendantEntity.builder().defendantName("Mr Jeff BRIDGES").build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenSingleString_whenGetSurname_thenReturn() {
        var defendantEntity = DefendantEntity.builder().defendantName("BRIDGES").build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenNullDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = DefendantEntity.builder().build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

    @Test
    void givenEmptyDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = DefendantEntity.builder().defendantName("   ").build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

}
