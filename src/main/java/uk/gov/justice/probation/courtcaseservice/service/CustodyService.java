package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.CustodyRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.OtherIds;
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
        if (convictionRestClient.getConviction(crn, convictionId)
                .map(Conviction::getCustodialType)
                .block() == null)
            return Mono.empty();


        final var nomsNumber = offenderRestClient.getOffender(crn)
                .map(CommunityApiOffenderResponse::getOtherIds)
                .map(OtherIds::getNomsNumber)
                .block();
        return custodyRestClient.getCustody(nomsNumber);
    }
}
