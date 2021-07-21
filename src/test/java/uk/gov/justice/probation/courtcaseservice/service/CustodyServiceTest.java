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
import uk.gov.justice.probation.courtcaseservice.service.exceptions.CustodyNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.ExpectedCustodyNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.NomsNumberNotAvailableException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustodyServiceTest {

    private static final String CRN = "CRN";
    private static final String NOMS_NUMBER = "nomnomnom";
    private static final long CONVICTION_ID = 12345L;
    @Mock
    private ConvictionRestClient convictionRestClient;
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private CustodyRestClient custodyRestClient;

    private CustodyService custodyService;

    private static final Conviction CONVICTION_WITH_CUSTODY = Conviction.builder()
            .custodialType(KeyValue.builder()
                    .code("LCK")
                    .description("Locked up")
                    .build())
            .build();

    private static final CommunityApiOffenderResponse OFFENDER_WITH_NOMS = CommunityApiOffenderResponse.builder()
            .otherIds(OtherIds.builder()
                    .nomsNumber(NOMS_NUMBER)
                    .build())
            .build();

    @BeforeEach
    public void setUp() {
        custodyService = new CustodyService(convictionRestClient, offenderRestClient, custodyRestClient);
    }

    @Test
    public void whenGetCustody_andCustodyDatesExist_thenReturnThem() {

        final var expectedCustody = Custody.builder().build();

        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(CONVICTION_WITH_CUSTODY));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_WITH_NOMS));
        when(custodyRestClient.getCustody(NOMS_NUMBER)).thenReturn(Mono.just(expectedCustody));
        final var actualCustody = custodyService.getCustody(CRN, CONVICTION_ID).block();

        assertThat(actualCustody).isEqualTo(expectedCustody);

    }

    @Test
    public void whenGetCustody_andOffenderNotInCustody_thenThrowNotFoundException() {
        final var conviction = Conviction.builder()
                .custodialType(null)
                .build();
        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(conviction));

        assertThatExceptionOfType(CustodyNotFoundException.class)
            .isThrownBy(() -> custodyService.getCustody(CRN, CONVICTION_ID).block())
            .withMessage("Offender with crn 'CRN' is not in custody for conviction '12345'");

    }

    @Test
    public void whenGetCustody_andCustodyDatesExist_andOffenderHasNoNomsNumber_thenThrowException() {
        final var offender = CommunityApiOffenderResponse.builder()
                .otherIds(OtherIds.builder()
                        .nomsNumber(null)
                        .build())
                .build();

        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(CONVICTION_WITH_CUSTODY));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(offender));
        assertThatExceptionOfType(NomsNumberNotAvailableException.class)
                .isThrownBy(
                        () -> custodyService.getCustody(CRN, CONVICTION_ID).block()
                ).withMessage("Could not get custody data as no NOMS number was returned from the community-api for crn 'CRN'");

    }

    @Test
    public void whenGetCustody_andOffenderHasNomsNumber_andNoCustodyDataReturned_thenThrowException() {

        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(CONVICTION_WITH_CUSTODY));
        when(offenderRestClient.getOffender(CRN)).thenReturn(Mono.just(OFFENDER_WITH_NOMS));
        when(custodyRestClient.getCustody(NOMS_NUMBER)).thenReturn(Mono.empty());
        assertThatExceptionOfType(ExpectedCustodyNotFoundException.class)
                .isThrownBy(
                        () -> custodyService.getCustody(CRN, CONVICTION_ID).block()
                ).withMessage("Expected custody data for nomsNumber 'nomnomnom' with custody type 'Locked up (LCK)' was not found at prison-api");

    }
}
