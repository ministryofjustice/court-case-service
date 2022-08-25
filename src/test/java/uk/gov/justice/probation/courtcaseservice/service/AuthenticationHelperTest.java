package uk.gov.justice.probation.courtcaseservice.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareAuthenticationToken;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static uk.gov.justice.probation.courtcaseservice.Constants.USER_UUID_CLAIM_NAME;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperTest {

    private static final String testUuid = "test-uuid";

    @Mock
    private AuthAwareAuthenticationToken principal;

    @Test
    void shouldGetUserUuidFromPrincipal() {
        given(principal.getTokenAttributes()).willReturn(Collections.singletonMap(USER_UUID_CLAIM_NAME, testUuid));

        Assertions.assertThat(new AuthenticationHelper().getAuthUserUuid(principal)).isEqualTo(testUuid);
    }
}