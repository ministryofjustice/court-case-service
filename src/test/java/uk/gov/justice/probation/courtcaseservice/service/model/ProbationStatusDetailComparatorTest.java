package uk.gov.justice.probation.courtcaseservice.service.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProbationStatusDetailComparatorTest {

    @Test
    void givenProbationStatusDetailIsSameAsExistingOne_ThenReturnFalse() {

        ProbationStatusDetail probationStatusDetail = ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .preSentenceActivity(true)
                .inBreach(true)
                .previouslyKnownTerminationDate(null)
                .inBreach(true)
                .build();

        ProbationStatusDetail probationStatusDetailToCompare = ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .preSentenceActivity(true)
                .inBreach(true)
                .previouslyKnownTerminationDate(null)
                .inBreach(true)
                .build();

        assertFalse(ProbationStatusDetailComparator.hasProbationStatusDetailsChanged(probationStatusDetail, probationStatusDetailToCompare));
    }

    @Test
    void givenProbationStatusDetailIsNotSameAsExistingOne_ThenReturnTrue() {

        ProbationStatusDetail probationStatusDetail = ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .preSentenceActivity(false)
                .inBreach(true)
                .previouslyKnownTerminationDate(null)
                .inBreach(true)
                .build();

        ProbationStatusDetail probationStatusDetailToCompare = ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .preSentenceActivity(true)
                .inBreach(true)
                .previouslyKnownTerminationDate(null)
                .inBreach(true)
                .build();

        assertTrue(ProbationStatusDetailComparator.hasProbationStatusDetailsChanged(probationStatusDetail, probationStatusDetailToCompare));
    }
}