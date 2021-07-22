package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class CustodyRestClientTest extends BaseIntTest {

    @Autowired
    private CustodyRestClient custodyClient;

    @Test
    public void whenGetCustody_thenReturnCustodyData() {
        final var custody = custodyClient.getCustody("G9542VP").block();

        assertThat(custody).isNotNull();
        assertThat(custody.getHomeDetentionCurfewActualDate()).isEqualTo(LocalDate.of(2021, 7, 16));
        assertThat(custody.getHomeDetentionCurfewEndDate()).isEqualTo(LocalDate.of(2021, 7, 17));
        assertThat(custody.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2021, 7, 18));
        assertThat(custody.getReleaseDate()).isEqualTo(LocalDate.of(2021, 7, 19));
        assertThat(custody.getTopupSupervisionStartDate()).isEqualTo(LocalDate.of(2021, 7, 20));
        assertThat(custody.getTopupSupervisionExpiryDate()).isEqualTo(LocalDate.of(2021, 7, 21));
    }

    @Test
    void givenServiceThrows404ThenThrowOffenderNotFoundException() {
        final var custody = custodyClient.getCustody("notknown").blockOptional();

        assertThat(custody).isEmpty();
    }

    @Test
    void givenServiceThrows500ThenThrow() {
        assertThatExceptionOfType(WebClientResponseException.class)
            .isThrownBy(() -> custodyClient.getCustody("G9542VQ").blockOptional());
    }
}
