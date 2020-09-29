package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * Compare convictions by their sentences. Priority described in English.
 *
 * <ol>
 *     <li>Active - to appear first. For active orders, sort so that most recent by sentence start date appear at the top of the list. Nulls last.</li>
 *     <li>Non-active - to appear after active. For non-active orders, sort so that most recent by termination date appear at the top of the list. Nulls last.</li>
 * </ol>
 *
 */
public class ConvictionBySentenceComparator implements Comparator<Conviction> {

    @Override
    public int compare(Conviction conviction, Conviction convictionOther) {
        if (Objects.equals(conviction.getActive(), convictionOther.getActive())) {
            if (Optional.ofNullable(conviction.getActive()).orElse(Boolean.FALSE)) {
                return convictionOther.getSentenceStartDate().orElse(LocalDate.MIN)
                    .compareTo(conviction.getSentenceStartDate().orElse(LocalDate.MIN));
            }
            else {
                return convictionOther.getSentenceTerminationDate().orElse(LocalDate.MIN)
                    .compareTo(conviction.getSentenceTerminationDate().orElse(LocalDate.MIN));
            }
        }
        else {
            return Optional.ofNullable(conviction.getActive())
                .map(active -> active ? -1 : 1)
                .orElse(-1);
        }
    }
}
