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
    }

    @Test
    void givenMatchedCaseWithMatches_whenGetProbationStatus_thenReturnNoRecord() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("No record");
    }

    @Test
    void givenMatchedCase_whenGetProbationStatus_thenReturnName() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .probationStatus(ProbationStatus.NOT_SENTENCED)
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("No record");
    }

    @Test
    void givenMatchedCase_whenGetProbationStatusCurrent_thenReturnName() {
        CourtCaseResponse response = CourtCaseResponse.builder()
            .crn("X340741")
            .probationStatus(ProbationStatus.CURRENT)
            .numberOfPossibleMatches(3)
            .build();

        assertThat(response.getProbationStatus()).isEqualTo("Current");
    }
}
