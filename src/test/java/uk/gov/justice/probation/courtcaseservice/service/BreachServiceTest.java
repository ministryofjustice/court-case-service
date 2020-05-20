package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiType;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class BreachServiceTest {
    public static final String CRN = "CRN";
    public static final long CONVICTION_ID = 12358073L;
    public static final long BREACH_ID = 1267523687L;

    @Mock
    private CommunityApiNsi nsi;
    @Mock
    private CommunityApiNsiType nsiType;
    @Mock
    private Conviction conviction;
    @Mock
    private NsiRestClient nsiRestClient;
    @Mock
    private ConvictionRestClient convictionRestClient;
    @Mock
    private NsiMapper nsiMapper;
    @Mock
    private BreachResponse expectedBreachResponse;
    private BreachService breachService;

    @BeforeEach
    public void setUp() {
        breachService = new BreachService(nsiRestClient, convictionRestClient, nsiMapper, Arrays.asList("BRE", "BRES"));
        when(nsi.getType()).thenReturn(nsiType);
        when(nsiRestClient.getNsiById(CRN, CONVICTION_ID, BREACH_ID)).thenReturn(Mono.just(nsi));
        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(conviction));
    }

    @Test
    public void whenGetBreachHasTypeBRE_thenReturnBreach() {
        when(nsiType.getCode()).thenReturn("BRE");
        when(nsiMapper.breachOf(nsi, conviction)).thenReturn(expectedBreachResponse);
        BreachResponse actualBreachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(actualBreachResponse).isEqualTo(expectedBreachResponse);
    }

    @Test
    public void whenGetBreachHasTypeBRES_thenReturnBreach() {
        when(nsiType.getCode()).thenReturn("BRES");
        when(nsiMapper.breachOf(nsi, conviction)).thenReturn(expectedBreachResponse);
        BreachResponse actualBreachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(actualBreachResponse).isEqualTo(expectedBreachResponse);
    }

    @Test
    public void whenNsiIsNotABreach_thenThrowNotFoundException() {
        when(nsiType.getCode()).thenReturn("NOTBRE");
        when(nsi.getNsiId()).thenReturn(BREACH_ID);

        assertThatExceptionOfType(NsiNotFoundException.class)
                .isThrownBy(() -> breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID))
                .withMessage("Breach with id '1267523687' does not exist");
    }
}