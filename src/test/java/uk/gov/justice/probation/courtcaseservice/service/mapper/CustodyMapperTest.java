package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentenceDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentencesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CustodyMapperTest {

    @Test
    public void shouldMapToCustody() {
        final var prisonApiSentencesResponse = PrisonApiSentencesResponse.builder()
                .sentenceDetail(PrisonApiSentenceDetail.builder()
                    .homeDetentionCurfewActualDate(zonedDateTimeOf(LocalDate.of(2021, 7, 20)))
                    .homeDetentionCurfewEndDate(zonedDateTimeOf(LocalDate.of(2021, 7, 21)))
                    .licenceExpiryDate(zonedDateTimeOf(LocalDate.of(2021, 7, 22)))
                    .releaseDate(zonedDateTimeOf(LocalDate.of(2021, 7, 23)))
                    .topupSupervisionStartDate(zonedDateTimeOf(LocalDate.of(2021, 7, 24)))
                    .topupSupervisionExpiryDate(zonedDateTimeOf(LocalDate.of(2021, 7, 25)))
                .build())
                .build();


        final var expectedCustody = Custody.builder()
                .homeDetentionCurfewActualDate(LocalDate.of(2021, 7, 20))
                .homeDetentionCurfewEndDate(LocalDate.of(2021, 7, 21))
                .licenceExpiryDate(LocalDate.of(2021, 7, 22))
                .releaseDate(LocalDate.of(2021, 7, 23))
                .topupSupervisionStartDate(LocalDate.of(2021, 7, 24))
                .topupSupervisionExpiryDate(LocalDate.of(2021, 7, 25))
                .build();

        final var actualCustody = CustodyMapper.custodyFrom(prisonApiSentencesResponse);

        assertThat(actualCustody).isEqualTo(expectedCustody);
    }

    @NonNull
    public ZonedDateTime zonedDateTimeOf(LocalDate of) {
        return ZonedDateTime.of(of, LocalTime.MIDNIGHT, ZoneId.systemDefault());
    }

    @Test
    public void shouldMapToNullsIfNoSentenceDetail() {
        final var prisonApiSentencesResponse = PrisonApiSentencesResponse.builder()
                .sentenceDetail(null)
                .build();


        final var expectedCustody = Custody.builder()
                .build();

        final var actualCustody = CustodyMapper.custodyFrom(prisonApiSentencesResponse);

        assertThat(actualCustody).isEqualTo(expectedCustody);
    }

}
