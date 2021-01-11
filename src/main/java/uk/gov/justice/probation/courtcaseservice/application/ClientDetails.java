package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.stereotype.Component;

/**
 * Client details extracted from incoming token before oauth token validation.
 *
 * Note that these details are Thread scoped and so must not be accessed by Reactor operations which may run on a
 * separate thread. If you need to access these values from within a reactive context, pass them using the Reactor
 * Context API.
 */
@Component
public class ClientDetails {
    private final static ThreadLocal<String> clientId = new ThreadLocal<>();
    private final static ThreadLocal<String> username = new ThreadLocal<>();

    public String getClientId(){
        return ClientDetails.clientId.get();
    }

    public String getUsername(){
        return ClientDetails.username.get();
    }

    public void setClientDetails(String clientId, String username) {
        ClientDetails.clientId.set(clientId);
        ClientDetails.username.set(username);
    }
}
