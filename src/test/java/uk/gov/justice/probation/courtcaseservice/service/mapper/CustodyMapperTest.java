package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentenceDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentencesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CustodyMapperTest {

    @Test
    public void shouldMapToCustody() {
        final var prisonApiSentencesResponse = PrisonApiSentencesResponse.builder()
                .sentenceDetail(PrisonApiSentenceDetail.builder()
                    .homeDetentionCurfewActualDate(LocalDate.of(2021, 7, 20))
                    .homeDetentionCurfewEndDate(LocalDate.of(2021, 7, 21))
                    .licenceExpiryDate(LocalDate.of(2021, 7, 22))
                    .releaseDate(LocalDate.of(2021, 7, 23))
                    .topupSupervisionStartDate(LocalDate.of(2021, 7, 24))
                    .topupSupervisionExpiryDate(LocalDate.of(2021, 7, 25))
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
