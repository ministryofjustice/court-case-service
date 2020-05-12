package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.BreachNotFoundException;

import java.util.Optional;

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
    private NsiRestClient restClient;
    @Mock
    private NsiMapper nsiMapper;
    @Mock
    private BreachResponse breachResponse;
    private BreachService breachService;

    @BeforeEach
    public void setUp() {
        breachService = new BreachService(restClient, nsiMapper);
    }

    @Test
    public void whenGetBreachExists_thenReturnBreach() {
        when(restClient.getNsiById(CRN, CONVICTION_ID, BREACH_ID)).thenReturn(Mono.just(nsi));
        when(nsiMapper.breachOf(nsi)).thenReturn(breachResponse);
        BreachResponse breachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(breachResponse).isEqualTo(breachResponse);
    }

    @Test
    public void whenGetBreachDoesNotExist_thenThrowNotFoundException() {
        when(restClient.getNsiById(CRN, CONVICTION_ID, BREACH_ID)).thenReturn(Mono.empty());
        assertThatExceptionOfType(BreachNotFoundException.class)
                .isThrownBy(() -> breachService.getBreach("CRN", 12358073L, 1267523687L))
                .withMessage("Breach with id 1267523687 does not exist");
    }
}