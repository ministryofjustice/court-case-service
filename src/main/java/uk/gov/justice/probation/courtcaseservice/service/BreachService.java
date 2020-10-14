package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtAppearance;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class BreachService {

    @Autowired
    private NsiRestClient nsiRestClient;
    @Autowired
    private ConvictionRestClient convictionRestClient;
    @Autowired
    private DocumentRestClient documentRestClient;
    @Autowired
    private OffenderRestClient offenderRestClient;

    @Value("#{'${community-api.nsis-filter.codes.breaches}'.split(',')}")
    private List<String> nsiBreachCodes;

    @Value("${community-api.sentence-appearance-code}")
    private String sentenceAppearanceCode;

    public BreachResponse getBreach(String crn, Long convictionId, Long breachId) {
        return Mono.zip(
                nsiRestClient.getNsiById(crn, convictionId, breachId),
                convictionRestClient.getConviction(crn, convictionId),
                documentRestClient.getDocumentsByCrn(crn),
                offenderRestClient.getOffenderCourtAppearances(crn, convictionId)
        )
                .map(tuple -> {
                    CommunityApiNsi nsi = tuple.getT1();
                    validateBreach(nsi);
                    return NsiMapper.breachOf(nsi, tuple.getT2(), tuple.getT3(), findLatestSentencingCourt(tuple.getT4()));
                })
                .block();
    }

    String findLatestSentencingCourt(List<CourtAppearance> courtAppearances) {
        return courtAppearances.stream()
            .filter(appearance -> sentenceAppearanceCode.equalsIgnoreCase(appearance.getType().getCode()))
            .max(Comparator.comparing(CourtAppearance::getDate))
            .map(CourtAppearance::getCourtName)
            .orElse(null);
    }


    private void validateBreach(CommunityApiNsi nsi) {
        if (!nsiBreachCodes.contains(nsi.getType().getCode()))
            throw new NsiNotFoundException(String.format("Breach with id '%s' does not exist", nsi.getNsiId()));
    }
}
