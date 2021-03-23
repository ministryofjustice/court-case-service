package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourtCaseResponseTest {

    @Test
    void givenUnmatchedCaseWithMatches_whenGetProbationStatus_thenReturnPossible() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("Possible nDelius record");
        assertThat(response.getProbationStatusActual()).isNull();
    }

    @Test
    void givenMatchedCaseWithMatches_whenGetProbationStatus_thenReturnNoRecord() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("No record");
        assertThat(response.getProbationStatusActual()).isNull();
    }

    @Test
    void givenMatchedCase_whenGetProbationStatus_thenReturnName() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .probationStatus(ProbationStatus.NOT_SENTENCED)
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("No record");
        assertThat(response.getProbationStatusActual()).isEqualTo("NOT_SENTENCED");
    }

    @Test
    void givenMatchedCase_whenGetProbationStatusCurrent_thenReturnName() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .probationStatus(ProbationStatus.CURRENT)
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("Current");
        assertThat(response.getProbationStatusActual()).isEqualTo("CURRENT");
    }

    @Test
    void givenMatchedCase_whenGetProbationStatusPreviouslyKnown_thenReturn() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .probationStatus(ProbationStatus.PREVIOUSLY_KNOWN)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("Previously known");
        assertThat(response.getProbationStatusActual()).isEqualTo("PREVIOUSLY_KNOWN");
    }
}
