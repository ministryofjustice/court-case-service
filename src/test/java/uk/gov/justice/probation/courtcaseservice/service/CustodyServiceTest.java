package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.CustodyRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustodyServiceTest {

    private static final String CRN = "CRN";
    private static final String NOMS_NUMBER = "nomnomnom";
    @Mock
    private ConvictionRestClient convictionRestClient;
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private CustodyRestClient custodyRestClient;

    private CustodyService custodyService;

    @BeforeEach
    public void setUp() {
        custodyService = new CustodyService(convictionRestClient, offenderRestClient, custodyRestClient);
    }

    @Test
    public void whenGetCustody_thenReturnIt() {
        final var conviction = Conviction.builder()
                .custodialType(KeyValue.builder()
                        .code("ANY")
                        .description("Owt will do")
                        .build())
                .build();

        final var offender = CommunityApiOffenderResponse.builder()
                .otherIds(OtherIds.builder()
                        .nomsNumber(NOMS_NUMBER)
                        .build())
                .build();
        final var expectedCustody = Custody.builder().build();

        when(convictionRestClient.getConviction(CRN, 12345L)).thenReturn(Mono.just(conviction));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(offender));
        when(custodyRestClient.getCustody(NOMS_NUMBER)).thenReturn(Mono.just(expectedCustody));
        final var actualCustody = custodyService.getCustody(CRN, 12345L).block();

        assertThat(actualCustody).isEqualTo(expectedCustody);

    }
}
