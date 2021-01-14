package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

public class CustomOAuth2ClientCredentialsGrantRequestEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {
    public RequestEntity enhanceWithUsername(OAuth2ClientCredentialsGrantRequest grantRequest, String username) {
        var request = super.convert(grantRequest);
        var headers = request.getHeaders();
        var formParameters = (MultiValueMap<String, Object>) request.getBody();
                formParameters.add("username", username);
        return new RequestEntity(formParameters, headers, HttpMethod.POST, request.getUrl());
    }
}
