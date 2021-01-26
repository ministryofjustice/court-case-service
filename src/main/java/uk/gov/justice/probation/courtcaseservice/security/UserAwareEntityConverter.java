package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

public class UserAwareEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {

    /**
     * This method relies on static fields managed by the Spring framework which can't be easily mocked and tested, it
     * also relies on implicit guarantees about the values returned by request.getBody(). In an effort to mitigate some
     * of this risk the class has been coded defensively with fine grained exception handling.
     */
    public RequestEntity<?> enhanceWithUsername(OAuth2ClientCredentialsGrantRequest grantRequest, String username) {
        if (grantRequest == null) {
            throw new UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as incoming request is null", username));
        }

        var request = super.convert(grantRequest);
        MultiValueMap<String, Object> formParameters;

        if (request == null) {
            throw new UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as request returned by super is null", username));
        }

        if (request.getType() == null || request.getType().equals(MultiValueMap.class)) {
            throw new UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as request type is %s", username, request.getType()));
        }

        try {
            formParameters = (MultiValueMap<String, Object>) request.getBody();
            formParameters.add("username", username);
        } catch (ClassCastException exception) {
            final var message = String.format("Unexpected condition - exception thrown when adding username '%s' to formParameters", username);
            throw new UnableToGetTokenOnBehalfOfUserException(message, exception);
        }

        return new RequestEntity<>(formParameters, request.getHeaders(), HttpMethod.POST, request.getUrl());
    }
}
