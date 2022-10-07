package uk.gov.justice.probation.courtcaseservice.service.model;

import java.util.Comparator;

import static java.util.Comparator.*;

public class ProbationStatusDetailComparator {

    static final Comparator<ProbationStatusDetail> probationStatusDetailComparator = Comparator.nullsFirst(comparing(ProbationStatusDetail::getStatus, nullsFirst(naturalOrder()))
            .thenComparing(ProbationStatusDetail::getPreviouslyKnownTerminationDate, nullsFirst(naturalOrder()))
            .thenComparing(ProbationStatusDetail::getInBreach, nullsFirst(naturalOrder()))
            .thenComparing(ProbationStatusDetail::isPreSentenceActivity, nullsFirst(naturalOrder()))
            .thenComparing(ProbationStatusDetail::getAwaitingPsr, nullsFirst(naturalOrder())));

    public static boolean hasProbationStatusDetailsChanged(ProbationStatusDetail probationStatusDetail, ProbationStatusDetail probationStatusDetailToCompare) {
        return probationStatusDetailComparator.compare(probationStatusDetail, probationStatusDetailToCompare) != 0;
    }
}
