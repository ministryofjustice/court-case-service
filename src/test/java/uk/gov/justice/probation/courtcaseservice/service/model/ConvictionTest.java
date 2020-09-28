package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvictionTest {

    @DisplayName("Get start date with no nulls")
    @Test
    void whenGetStartDate_thenReturn() {
        Conviction conviction = Conviction.builder()
                    .sentence(Sentence.builder()
                        .startDate(LocalDate.now())
                        .build())
            .build();

        assertThat(conviction.getSentenceStartDate()).isPresent();
    }

    @DisplayName("Start date with no sentence, should return empty optional")
    @Test
    void givenNullSentence_whenGetStartDate_thenReturnEmpty() {
        assertThat(Conviction.builder().build().getSentenceStartDate()).isNotPresent();
    }

    @DisplayName("Start date with a sentence but no start date, should return empty optional")
    @Test
    void givenSentenceWithNullStartDate_whenGetStartDate_thenReturnEmpty() {
        Conviction conviction = Conviction.builder()
            .sentence(Sentence.builder().build())
            .build();

        assertThat(conviction.getSentenceStartDate()).isNotPresent();
    }

    @DisplayName("Get termination date with no nulls")
    @Test
    void whenGetTerminationDate_thenReturn() {
        Conviction conviction = Conviction.builder()
            .sentence(Sentence.builder()
                .terminationDate(LocalDate.now())
                .build())
            .build();

        assertThat(conviction.getSentenceTerminationDate()).isPresent();
    }

    @DisplayName("Termination date with no sentence, should return empty optional")
    @Test
    void givenNullSentence_whenGetTerminationDate_thenReturnEmpty() {
        assertThat(Conviction.builder().build().getSentenceTerminationDate()).isNotPresent();
    }

    @DisplayName("Start date with a sentence but no termination date, should return empty optional")
    @Test
    void givenSentenceWithNullTerminationDate_whenGetStartDate_thenReturnEmpty() {
        Conviction conviction = Conviction.builder()
            .sentence(Sentence.builder().build())
            .build();

        assertThat(conviction.getSentenceTerminationDate()).isNotPresent();
    }

    @DisplayName("Invoke the Conviction object's compareTo implementation to sort by ID ascending")
    @Test
    void whenSort_ThenReturnByIdAsc() {
        Conviction conviction1 = Conviction.builder().convictionId("123450").build();
        Conviction conviction2 = Conviction.builder().convictionId("99999").build();
        Conviction conviction3 = Conviction.builder().convictionId("12345").build();

        List<Conviction> convictions = Stream.of(conviction2, conviction3, conviction1)
            .sorted()
            .collect(Collectors.toList());

        assertThat(convictions).containsExactly(conviction3, conviction2, conviction1);
    }
}
