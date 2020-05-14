package uk.gov.justice.probation.courtcaseservice.restclient;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class NsiRestClientIntTest {
    private static final String CRN = "X320741";
    private static final long CONVICTION_ID = 2500295343L;
    @Autowired
    private NsiRestClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void givenNsiExists_whenGetNsi_thenReturnIt() {
        Mono<CommunityApiNsi> mono = client.getNsiById(CRN, CONVICTION_ID, 2500003903L);

        assertThat(mono.blockOptional()).isPresent();

    }

    @Test
    public void givenNsiDoesNotExist_whenGetNsi_thenReturnEmpty() {
        assertThatExceptionOfType(NsiNotFoundException.class)
            .isThrownBy(() -> client.getNsiById(CRN, CONVICTION_ID, 1230045000L).block())
            .withMessage("Nsi with id '1230045000' not found for convictionId '2500295343' and crn 'X320741'");
    }
}