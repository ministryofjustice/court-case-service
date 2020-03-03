package uk.gov.justice.probation.courtcaseservice.restclient;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import java.time.LocalDate;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OffenderRestClientIntTest {

    private static final String CRN = "X320741";
    @Autowired
    private OffenderRestClient offenderRestClient;


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));


    @Test
    public void whenGetOffenderByCrnCalled_thenMakeRestCallToCommunityApi() {
        var optionalOffender = offenderRestClient.getOffenderByCrn(CRN).blockOptional();

        assertThat(optionalOffender).isNotEmpty();
        var offender = optionalOffender.get();
        assertThat(offender.getCrn()).isEqualTo(CRN);

        assertThat(offender.getOffenderManager().getForenames()).isEqualTo("Temperance");
        assertThat(offender.getOffenderManager().getSurname()).isEqualTo("Brennan");
        assertThat(offender.getOffenderManager().getAllocatedDate()).isEqualTo(LocalDate.of(2019,9,30));

        // TODO: Test convictions values returned
    }

    @Test
    public void givenOffenderDoesNotExist_whenGetOffenderByCrnCalled_ReturnEmpty() {
        Optional<Offender> offender = offenderRestClient.getOffenderByCrn("NOT THERE").blockOptional();
        assertThat(offender).isEmpty();
    }
}