package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken;

import java.security.Principal;

import static uk.gov.justice.probation.courtcaseservice.Constants.*;

@Component
public class AuthenticationHelper {
    public String getAuthUserUuid(Principal principal) {
        return ((AuthAwareAuthenticationToken)principal).getTokenAttributes().get(USER_UUID_CLAIM_NAME).toString();
    }

    public String getAuthUserId(Principal principal) {
        return ((AuthAwareAuthenticationToken)principal).getTokenAttributes().get(USER_ID_CLAIM_NAME).toString();
    }

    public String getAuthUserName(Principal principal) {
        return ((AuthAwareAuthenticationToken)principal).getTokenAttributes().get(USER_NAME_CLAIM_NAME).toString();
    }

    public String getAuthSource(Principal principal) {
        return ((AuthAwareAuthenticationToken)principal).getTokenAttributes().get(AUTH_SOURCE_CLAIM_NAME).toString();
    }
}
