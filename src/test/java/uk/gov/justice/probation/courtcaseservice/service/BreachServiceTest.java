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
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class BreachServiceTest {
    public static final String CRN = "CRN";
    public static final long CONVICTION_ID = 12358073L;
    public static final long BREACH_ID = 1267523687L;

    @Mock
    private CommunityApiNsi nsi;
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
        breachService = new BreachService(nsiRestClient, convictionRestClient, nsiMapper);
    }

    @Test
    public void whenGetBreachExists_thenReturnBreach() {
        when(nsiRestClient.getNsiById(CRN, CONVICTION_ID, BREACH_ID)).thenReturn(Mono.just(nsi));
        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(nsiMapper.breachOf(nsi, conviction)).thenReturn(expectedBreachResponse);
        BreachResponse actualBreachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(actualBreachResponse).isEqualTo(expectedBreachResponse);
    }
}