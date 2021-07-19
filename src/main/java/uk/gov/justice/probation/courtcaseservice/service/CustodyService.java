package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.CustodyRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.ExpectedCustodyNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.NomsNumberNotAvailableException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

@AllArgsConstructor
public class CustodyService {
    private final ConvictionRestClient convictionRestClient;
    private final OffenderRestClient offenderRestClient;
    private final CustodyRestClient custodyRestClient;

    @Autowired
    public CustodyService(ConvictionRestClient convictionRestClient, OffenderRestClientFactory offenderRestClientFactory, CustodyRestClient custodyRestClient) {
        this.convictionRestClient = convictionRestClient;
        this.custodyRestClient = custodyRestClient;
        this.offenderRestClient = offenderRestClientFactory.build();
    }

    public Mono<Custody> getCustody(String crn, Long convictionId) {

        final var inCustodyMono = convictionRestClient.getConviction(crn, convictionId)
                .mapNotNull(Conviction::getCustodialType);

        final var nomsMono = offenderRestClient.getOffender(crn)
                .map(CommunityApiOffenderResponse::getOtherIds)
                .mapNotNull(OtherIds::getNomsNumber)
                .switchIfEmpty(Mono.error(new NomsNumberNotAvailableException(String.format("Could not get custody data as no NOMS number was returned from the community-api for crn '%s'", crn))));

        return Mono.zip(inCustodyMono, nomsMono)
                // We only care that T1 is not empty so can discard the value
                .map(Tuple2::getT2)
                .flatMap(nomsNumber -> custodyRestClient.getCustody(nomsNumber)
                        .switchIfEmpty(Mono.error(new ExpectedCustodyNotFoundException(String.format("Expected custody data for nomsNumber '%s' was not found at prison-api", nomsNumber)))));
    }
}
