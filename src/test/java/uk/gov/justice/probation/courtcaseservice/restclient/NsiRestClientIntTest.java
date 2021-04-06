package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class NsiRestClientIntTest extends BaseIntTest {
    private static final String CRN = "X320741";
    private static final long CONVICTION_ID = 2500295343L;
    public static final long NSI_ID = 2500003903L;
    @Autowired
    private NsiRestClient client;

    @Test
    void givenNsiExists_whenGetNsi_thenReturnIt() {
        Mono<CommunityApiNsi> mono = client.getNsiById(CRN, CONVICTION_ID, NSI_ID);

        assertThat(mono.blockOptional()).isPresent();

    }

    @Test
    void givenNsiDoesNotExist_whenGetNsi_thenReturnThrowNotFound() {
        assertThatExceptionOfType(NsiNotFoundException.class)
            .isThrownBy(() -> client.getNsiById(CRN, CONVICTION_ID, 1230045000L).block())
            .withMessage("Nsi with id '1230045000' not found for convictionId '2500295343' and crn 'X320741'");
    }

    @Test
    void givenServerError_whenGetNsi_thenThrow() {
        assertThatExceptionOfType(WebClientResponseException.class)
                .isThrownBy(() -> client.getNsiById(CRN, CONVICTION_ID, 500L).block());
    }
}
