package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions.BreachMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class OffenderRestClient {
    @Value("${community-api.offender-by-crn-url-template}")
    private String offenderUrlTemplate;
    @Value("${community-api.convictions-by-crn-url-template}")
    private String convictionsUrlTemplate;
    @Value("${community-api.requirements-by-crn-url-template}")
    private String requirementsUrlTemplate;
    @Value("${community-api.nsis-url-template}")
    private String nsisTemplate;
    @Value("${community-api.nsis-filter.codes.queryParameter}")
    private String nsiCodesParam;
    @Value("#{'${community-api.nsis-filter.codes.breaches}'.split(',')}")
    private List<String> nsiBreachCodes;

    @Autowired
    private OffenderMapper mapper;
    @Autowired
    private BreachMapper breachMapper;
    @Autowired
    @Qualifier("communityApiClient")
    private RestClientHelper clientHelper;

    public Mono<ProbationRecord> getProbationRecordByCrn(String crn) {
        return clientHelper.get(String.format(offenderUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender data for CRN '%s'", crn), e))
                .map(offender -> mapper.probationRecordFrom(offender));
    }

    public Mono<List<Conviction>> getConvictionsByCrn(String crn) {
        return clientHelper.get(String.format(convictionsUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiConvictionsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving convictions data for CRN '%s'", crn), e))
                .map( convictionsResponse -> mapper.convictionsFrom(convictionsResponse));
    }

    public Mono<List<Breach>> getBreaches(String crn, String convictionId) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll(nsiCodesParam, nsiBreachCodes);
        return clientHelper.get(String.format(nsisTemplate, crn, convictionId), params)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, resp -> clientHelper.handleError(crn, resp))
                .bodyToMono(CommunityApiNsiResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving breaches data for CRN '%s' and conviction id '%s'", crn, convictionId), e))
                .map(resp -> breachMapper.breachesFrom(resp));
    }

    public Mono<List<Requirement>> getConvictionRequirements(String crn, String convictionId) {
        return clientHelper.get(String.format(requirementsUrlTemplate, crn, convictionId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiRequirementsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving requirements data for CONVICTIONID '%s'", convictionId), e))
                .map( requirementsResponse -> mapper.requirementsFrom(requirementsResponse));
    }

}
