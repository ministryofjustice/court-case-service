package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.CourtAppearanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.RegistrationMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.RequirementMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions.BreachMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtAppearancesResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiLicenceConditionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRegistrationsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtAppearance;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class OffenderRestClient {
    @Value("${community-api.offender-by-crn-url-template}")
    private String offenderUrlTemplate;
    @Value("${community-api.offender-by-crn-all-url-template}")
    private String offenderAllUrlTemplate;
    @Value("${community-api.convictions-by-crn-url-template}")
    private String convictionsUrlTemplate;
    @Value("${community-api.requirements-by-crn-url-template}")
    private String requirementsUrlTemplate;
    @Value("${community-api.pss-requirements-by-crn-and-conviction-url-template}")
    private String pssRequirementsUrlTemplate;
    @Value("${community-api.licence-conditions-by-crn-and-conviction-url-template}")
    private String licenceConditionsUrlTemplate;
    @Value("${community-api.registrations-by-crn-url-template}")
    private String registrationsUrlTemplate;
    @Value("${community-api.nsis-url-template}")
    private String nsisTemplate;
    @Value("${community-api.court-appearances-by-crn-and-nsi-url-template}")
    private String courtAppearancesTemplate;
    @Value("${community-api.probation-status-by-crn}")
    private String probationStatusTemplate;

    @Value("${community-api.nsis-filter.codes.queryParameter}")
    private String nsiCodesParam;
    @Value("#{'${community-api.nsis-filter.codes.breaches}'.split(',')}")
    private List<String> nsiBreachCodes;
    @Value("${community-api.offender-address-code}")
    private String addressCode;
    private RestClientHelper clientHelper;

    public Mono<ProbationRecord> getProbationRecordByCrn(String crn) {
        return clientHelper.get(String.format(offenderAllUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender probation record data for CRN '%s'", crn), e))
                .map(OffenderMapper::probationRecordFrom);
    }

    public Mono<ProbationStatusDetail> getProbationStatusByCrn(String crn) {
        return clientHelper.get(String.format(probationStatusTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse)-> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiProbationStatusDetail.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender probation status data for CRN '%s'", crn), e))
            .map(OffenderMapper::probationStatusDetailFrom);
    }

    public Mono<CommunityApiOffenderResponse> getOffender(String crn) {
        return clientHelper.get(String.format(offenderUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiOffenderResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender detail data for CRN '%s'", crn), e));
    }

    public Mono<OffenderMatchDetail> getOffenderMatchDetailByCrn(String crn) {
        return clientHelper.get(String.format(offenderAllUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> Mono.empty())
            .bodyToMono(CommunityApiOffenderResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender match detail data for CRN '%s'", crn), e))
            .map(offender -> OffenderMapper.offenderMatchDetailFrom(offender, addressCode));
    }

    public Mono<List<Conviction>> getConvictionsByCrn(String crn) {
        return clientHelper.get(String.format(convictionsUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
                .bodyToMono(CommunityApiConvictionsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving convictions data for CRN '%s'", crn), e))
                .map(OffenderMapper::convictionsFrom);
    }

    public Mono<List<Breach>> getBreaches(String crn, String convictionId) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll(nsiCodesParam, nsiBreachCodes);
        return clientHelper.get(String.format(nsisTemplate, crn, convictionId), params)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, resp -> clientHelper.handleConvictionError(crn, Long.valueOf(convictionId), resp))
                .bodyToMono(CommunityApiNsiResponse.class)
                .onErrorMap(e1 -> {
                    log.error(String.format("Unexpected exception when retrieving breaches data for CRN '%s' and conviction id '%s'", crn, convictionId), e1);
                    return e1;
                })
                .map(BreachMapper::breachesFrom);
    }

    public Mono<List<Requirement>> getConvictionRequirements(String crn, Long convictionId) {
        return clientHelper.get(String.format(requirementsUrlTemplate, crn, convictionId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
                .bodyToMono(CommunityApiRequirementsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving requirements data for CONVICTIONID '%s'", convictionId), e))
                .map(RequirementMapper::requirementsFrom)
                .onErrorReturn(Collections.emptyList());
    }

    public Mono<List<PssRequirement>> getConvictionPssRequirements(String crn, Long convictionId) {
        return clientHelper.get(String.format(pssRequirementsUrlTemplate, crn, convictionId))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiPssRequirementsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving PSS requirements data for CONVICTIONID '%s'", convictionId), e))
            .map(RequirementMapper::pssRequirementsFrom)
            .onErrorReturn(Collections.emptyList());
    }

    public Mono<List<LicenceCondition>> getConvictionLicenceConditions(String crn, Long convictionId) {
        return clientHelper.get(String.format(licenceConditionsUrlTemplate, crn, convictionId))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiLicenceConditionsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving licence conditions data for CONVICTIONID '%s'", convictionId), e))
            .map(RequirementMapper::licenceConditionsFrom)
            .onErrorReturn(Collections.emptyList());
    }

    public Mono<List<Registration>> getOffenderRegistrations(String crn) {
        return clientHelper.get(String.format(registrationsUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiRegistrationsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when registration data for CRN '%s'", crn), e))
            .map(RegistrationMapper::registrationsFrom);
    }

    public Mono<List<CourtAppearance>> getOffenderCourtAppearances(String crn, Long convictionId) {
        return clientHelper.get(String.format(courtAppearancesTemplate, crn, convictionId))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiCourtAppearancesResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when fetching court appearances for CRN '%s' with conviction ID '%s'", crn, convictionId), e))
            .map(CourtAppearanceMapper::appearancesFrom);
    }
}
