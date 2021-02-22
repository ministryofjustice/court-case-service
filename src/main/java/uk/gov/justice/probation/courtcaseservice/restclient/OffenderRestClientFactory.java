package uk.gov.justice.probation.courtcaseservice.restclient;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.application.WebClientFactory;
import uk.gov.justice.probation.courtcaseservice.security.UnableToGetTokenOnBehalfOfUserException;

import java.util.List;

@Component
@AllArgsConstructor
public class OffenderRestClientFactory {
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
    @Value("#{'${community-api.mandated-username-client-ids}'.split(',')}")
    private List<String> mandatedUsernameClientIds;

    private final WebClientFactory webClientFactory;
    private ClientDetails clientDetails;

    @Autowired
    public OffenderRestClientFactory(WebClientFactory webClientFactory, ClientDetails clientDetails) {
        this.webClientFactory = webClientFactory;
        this.clientDetails = clientDetails;
    }

    /**
     * Create a new OffenderRestClient
     *
     * Important:
     * This factory method *must* be called from a @RequestScoped bean when creating a new OffenderRestClient, do not use
     * Spring dependency injection. This allows the client to be instantiated with the user details of the current user.
     * Invoking this method from any other context will result in the username not being passed to the community-api
     * meaning access restrictions will not be enforced for the user.
     *
     * @return An OffenderRestClient authenticated on behalf of the current user
     */
    public OffenderRestClient build() {
        if(mandatedUsernameClientIds.contains(clientDetails.getClientId()) && StringUtil.isNullOrEmpty(clientDetails.getUsername())) {
            final var message = String.format("Unable to request client-credentials grant for service call as username was not provided " +
                    "in the incoming token and username is mandatory for clientId '%s'", clientDetails.getClientId());
            throw new UnableToGetTokenOnBehalfOfUserException(message);
        }
        final var restClientHelper = webClientFactory.buildCommunityRestClientHelper(clientDetails.getUsername());
        return new OffenderRestClient(offenderUrlTemplate, offenderAllUrlTemplate, convictionsUrlTemplate, requirementsUrlTemplate, pssRequirementsUrlTemplate, licenceConditionsUrlTemplate, registrationsUrlTemplate, nsisTemplate, courtAppearancesTemplate, probationStatusTemplate, nsiCodesParam, nsiBreachCodes, addressCode, restClientHelper);
    }
}
