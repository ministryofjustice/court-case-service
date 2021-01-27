package uk.gov.justice.probation.courtcaseservice.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Client details extracted from incoming token before oauth token validation.
 *
 * Note that these details are Thread scoped and so must not be accessed by Reactor operations which may run on a
 * separate thread. If you need to access these values from within a reactive context, pass them using the Reactor
 * Context API.
 */
@Component
@Slf4j
public class ClientDetails implements AuditorAware<String> {
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

    @Override
    public Optional<String> getCurrentAuditor() {
        if (clientId.get() == null && username.get() == null){
            log.warn("Unable to retrieve clientId or username from ClientDetails, getCurrentAuditor() may have been " +
                    "called from an asynchronous Reactor context");
            return Optional.empty();
        }
        return Optional.ofNullable(String.format("%s(%s)",
                Optional.ofNullable(username.get()).orElse(""),
                Optional.ofNullable(clientId.get()).orElse("")));
    }
}
