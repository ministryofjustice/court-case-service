package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvictionBySentenceComparatorTest {

    private static final LocalDate EARLIEST = LocalDate.of(2019, Month.JULY, 29);
    private static final LocalDate MIDDLE = LocalDate.of(2019, Month.JULY, 30);
    private static final LocalDate LATEST = LocalDate.of(2019, Month.JULY, 31);

    private static ConvictionBySentenceComparator comparator;

    @BeforeAll
    static void beforeAll() {
        comparator = new ConvictionBySentenceComparator();
    }

    @DisplayName("Comparing active convictions, use sentence start date")
    @Test
    void givenAllActive_whenSort_thenMostRecentStartDateFirst() {
        Conviction conviction1 = buildConviction("1", LATEST, null, Boolean.TRUE);
        Conviction conviction2 = buildConviction("2", EARLIEST, null, Boolean.TRUE);

        List<Conviction> convictions = Stream.of(conviction2, conviction1)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2);
    }

    @DisplayName("Comparing active convictions, use sentence start date with null date at end")
    @Test
    void givenAllActiveWithNullDate_whenSort_thenMostRecentStartDateFirst() {
        Conviction conviction1 = buildConviction("1", EARLIEST, null, Boolean.TRUE);
        Conviction conviction2 = buildConviction("2", null, null, Boolean.TRUE);

        List<Conviction> convictions = Stream.of(conviction2, conviction1)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2);
    }

    @DisplayName("Comparing non-active convictions, use sentence termination date with most recent first")
    @Test
    void givenAllNonActive_whenSort_thenMostRecentTerminationDateFirst() {

        Conviction conviction1 = buildConviction("1", EARLIEST, LATEST, Boolean.FALSE);
        Conviction conviction2 = buildConviction("2", LATEST, MIDDLE, Boolean.FALSE);
        Conviction conviction3 = buildConviction("3", LATEST, EARLIEST, Boolean.FALSE);

        List<Conviction> convictions = Stream.of(conviction2, conviction1, conviction3)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2, conviction3);
    }

    @DisplayName("Comparing active convictions, use sentence start date with null date at end")
    @Test
    void givenNullSentenceInActive_whenSort_thenReturnSentenceFirst() {

        Conviction conviction1 = buildConviction("2", EARLIEST, LATEST, Boolean.TRUE);
        Conviction conviction2 = Conviction.builder().convictionId("1").active(Boolean.TRUE).build();
        Conviction conviction3 = buildConviction("3", EARLIEST, LATEST, Boolean.FALSE);

        List<Conviction> convictions = Stream.of(conviction2, conviction1, conviction3)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2, conviction3);
    }

    @DisplayName("Comparing mix of active convictions")
    @Test
    void givenOneNullActive_whenSort_thenActiveFirstTreatNullAsNonActive() {
        // 1 first because active, despite old dates. 2 considered non-active and next because most recent
        Conviction conviction1 = buildConviction("1", EARLIEST, EARLIEST, Boolean.TRUE);
        Conviction conviction2 = buildConviction("2", LATEST, LATEST, null);
        Conviction conviction3 = buildConviction("3", LATEST, EARLIEST, Boolean.FALSE);

        List<Conviction> convictions = Stream.of(conviction3, conviction2, conviction1)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2, conviction3);
    }

    @DisplayName("Comparing mix of active convictions")
    @Test
    void givenAllNullActive_whenSort_thenSortByTerminationDate() {
        //All non-active null, sort by
        Conviction conviction1 = buildConviction("1", EARLIEST, LATEST, null);
        Conviction conviction2 = buildConviction("2", LATEST, EARLIEST, null);
        Conviction conviction3 = buildConviction("3", LATEST, null, null);

        List<Conviction> convictions = Stream.of(conviction3, conviction2, conviction1)
            .sorted(comparator)
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction1, conviction2, conviction3);
    }

    private Conviction buildConviction(String convictionId, LocalDate sentenceStartDate, LocalDate sentenceTerminationDate, Boolean active) {
        return Conviction.builder()
            .active(active)
            .convictionId(convictionId)
            .sentence(Sentence.builder()
                .startDate(sentenceStartDate)
                .terminationDate(sentenceTerminationDate)
                .build())
            .build();
    }


}
