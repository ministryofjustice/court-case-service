package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.CustodyRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.OtherIds;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.CustodyNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.ExpectedCustodyNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.NomsNumberNotAvailableException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@AllArgsConstructor
@RequestScope
@Service
public class CustodyService {
    private static final String NOMS_NOT_AVAILABLE_MESSAGE = "Could not get custody data as no NOMS number was returned from the community-api for crn '%s'";
    private static final String CUSTODY_NOT_FOUND_MESSAGE = "Expected custody data for nomsNumber '%s' with custody type '%s (%s)' was not found at prison-api";

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

        return getCustodyType(crn,convictionId)
                .zipWhen((ignored) -> getNomsNumber(crn))
                .flatMap(tuple2 -> getCustody(tuple2.getT2(), tuple2.getT1()));
    }

    private Mono<Custody> getCustody(String nomsNumber, KeyValue custodyType) {
        return custodyRestClient.getCustody(nomsNumber)
                .switchIfEmpty(Mono.error(new ExpectedCustodyNotFoundException(
                        String.format(CUSTODY_NOT_FOUND_MESSAGE,
                                nomsNumber,
                                custodyType.getDescription(),
                                custodyType.getCode()
                        ))));
    }

    private Mono<String> getNomsNumber(String crn) {
        return offenderRestClient.getOffender(crn)
                .map(CommunityApiOffenderResponse::getOtherIds)
                .mapNotNull(OtherIds::getNomsNumber)
                .switchIfEmpty(Mono.error(new NomsNumberNotAvailableException(String.format(NOMS_NOT_AVAILABLE_MESSAGE, crn))));
    }

    private Mono<KeyValue> getCustodyType(String crn, Long convictionId) {
        return convictionRestClient.getConviction(crn, convictionId)
                .mapNotNull(Conviction::getCustodialType)
                .switchIfEmpty(Mono.error(new CustodyNotFoundException(String.format("Offender with crn '%s' is not in custody for conviction '%s'", crn, convictionId))));
    }
}
