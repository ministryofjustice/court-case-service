package uk.gov.justice.probation.courtcaseservice.service.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustodialStatusTest {

    @Test
    void whenHaveCode_thenReturnEnum() {
        assertThat(CustodialStatus.fromString("P")).isSameAs(CustodialStatus.POST_SENTENCE_SUPERVISION);
        assertThat(CustodialStatus.fromString("B")).isSameAs(CustodialStatus.RELEASED_ON_LICENCE);
    }

    @Test
    void whenHaveIncorrectCode_thenReturnNull() {
        assertThat(CustodialStatus.fromString("X")).isNull();
    }

}
