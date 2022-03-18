package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER_ENTITY;

class PhoneNumberTest {

    @Test
    void shouldMapToEntityOnAsEntity() {
        assertThat(DEFENDANT_PHONE_NUMBER.asEntity()).isEqualTo(DEFENDANT_PHONE_NUMBER_ENTITY);
    }

    @Test
    void shouldMapToFromEntity() {
        assertThat(PhoneNumber.of(DEFENDANT_PHONE_NUMBER_ENTITY)).isEqualTo(DEFENDANT_PHONE_NUMBER);
    }
}