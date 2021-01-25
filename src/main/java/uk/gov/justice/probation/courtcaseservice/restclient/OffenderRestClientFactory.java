package uk.gov.justice.probation.courtcaseservice.restclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.security.WebClientFactory;

import java.util.List;

@Component
public class OffenderRestClientFactory {
    private final WebClientFactory webClientFactory;

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

    @Value("${community-api.nsis-filter.codes.queryParameter}")
    private String nsiCodesParam;
    @Value("#{'${community-api.nsis-filter.codes.breaches}'.split(',')}")
    private List<String> nsiBreachCodes;
    @Value("${community-api.offender-address-code}")
    private String addressCode;

    public OffenderRestClientFactory(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    public OffenderRestClient build() {
        final var restClientHelper = webClientFactory.buildCommunityApiClient();
        return new OffenderRestClient(offenderUrlTemplate, offenderAllUrlTemplate, convictionsUrlTemplate, requirementsUrlTemplate, pssRequirementsUrlTemplate, licenceConditionsUrlTemplate, registrationsUrlTemplate, nsisTemplate, courtAppearancesTemplate, nsiCodesParam, nsiBreachCodes, addressCode, restClientHelper);
    }
}
