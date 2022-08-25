package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken;

import java.security.Principal;

import static uk.gov.justice.probation.courtcaseservice.Constants.USER_UUID_CLAIM_NAME;

@Component
public class AuthenticationHelper {
    public String getAuthUserUuid(Principal principal) {
        return ((AuthAwareAuthenticationToken)principal).getTokenAttributes().get(USER_UUID_CLAIM_NAME).toString();
    }
}
