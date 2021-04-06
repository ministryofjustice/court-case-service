package uk.gov.justice.probation.courtcaseservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class UserAwareEntityConverterIntTest extends BaseIntTest {

    private UserAwareEntityConverter converter;
    private OAuth2ClientCredentialsGrantRequest request;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    public void setUp() {
        converter = new UserAwareEntityConverter();
        request = new OAuth2ClientCredentialsGrantRequest(clientRegistrationRepository.findByRegistrationId("community-api-client"));
    }

    @Test
    void shouldAddUsernameToReturnedRequest() {
        var requestEntity = converter.enhanceWithUsername(request, "test user");
        var body = (MultiValueMap<String, Object>) requestEntity.getBody();
        assertThat(body.get("username").get(0)).isEqualTo("test user");
    }

    @Test
    void shouldThrowExceptionIfRequestIsNull() {
        assertThatExceptionOfType(UnableToGetTokenOnBehalfOfUserException.class)
            .isThrownBy( () -> converter.enhanceWithUsername(null, "test user"))
            .withMessage("Unexpected condition - cannot add username 'test user' to formParameters as incoming request is null");
    }
}
