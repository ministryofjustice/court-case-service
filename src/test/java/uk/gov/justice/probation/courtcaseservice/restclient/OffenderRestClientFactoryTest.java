package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.application.WebClientFactory;
import uk.gov.justice.probation.courtcaseservice.security.UnableToGetTokenOnBehalfOfUserException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffenderRestClientFactoryTest {
    @Mock
    private WebClientFactory webClientFactory;
    @Mock
    private ClientDetails clientDetails;
    private OffenderRestClientFactory clientFactory;
    private final List<String> nsiBreachCodes = Collections.singletonList("BRE");
    private final List<String> mandatesUsernameClientIds = Collections.singletonList("mandatory-username");
    @Mock
    private RestClientHelper restClientHelper;

    @BeforeEach
    public void setUp() {
        clientFactory = new OffenderRestClientFactory(
                "offenderUrlTemplate",
                "offenderAllUrlTemplate",
                "offenderManagersUrlTemplate",
                "convictionsUrlTemplate",
                "requirementsUrlTemplate",
                "pssRequirementsUrlTemplate",
                "licenceConditionsUrlTemplate",
                "registrationsUrlTemplate",
                "nsisTemplate",
                "courtAppearancesTemplate",
                "probationStatusTemplate",
                "nsiCodesParam",
                nsiBreachCodes,
                "addressCode",
                mandatesUsernameClientIds,
                webClientFactory,
                clientDetails,
                restClientHelper
                );
    }

    @Test
    public void givenClientIdIsNull_andUsernameAvailable_whenBuild_thenPassUsername() {
        when(clientDetails.getClientId()).thenReturn(null);
        when(clientDetails.getUsername()).thenReturn("username");
        clientFactory.buildUserAwareOffenderRestClient();
        verify(webClientFactory).buildCommunityRestClientHelper("username");
        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdIsNull_andUsernameIsNull_whenBuild_thenPassNullUsername() {
        when(clientDetails.getClientId()).thenReturn(null);
        when(clientDetails.getUsername()).thenReturn(null);
        clientFactory.buildUserAwareOffenderRestClient();
        verify(webClientFactory).buildCommunityRestClientHelper(null);
        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdNotInRequireUsernameList_andUsernameIsNull_whenBuild_thenPassNullUsername() {
        when(clientDetails.getClientId()).thenReturn("some other client id");
        when(clientDetails.getUsername()).thenReturn(null);
        clientFactory.buildUserAwareOffenderRestClient();
        verify(webClientFactory).buildCommunityRestClientHelper(null);
        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdNotInRequireUsernameList_andUsernameAvailable_whenBuild_thenPassUsername() {
        when(clientDetails.getClientId()).thenReturn("some other client id");
        when(clientDetails.getUsername()).thenReturn("username");
        clientFactory.buildUserAwareOffenderRestClient();
        verify(webClientFactory).buildCommunityRestClientHelper("username");
        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdInRequireUsernameList_andUsernameAvailable_whenBuild_thenPassUsername() {
        when(clientDetails.getClientId()).thenReturn("mandatory-username");
        when(clientDetails.getUsername()).thenReturn("username");
        clientFactory.buildUserAwareOffenderRestClient();
        verify(webClientFactory).buildCommunityRestClientHelper("username");
        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdIsInRequireUsernameList_andUsernameIsNull_whenBuild_thenThrowException() {
        when(clientDetails.getClientId()).thenReturn("mandatory-username");
        when(clientDetails.getUsername()).thenReturn(null);

        assertThatExceptionOfType(UnableToGetTokenOnBehalfOfUserException.class)
                .isThrownBy(() -> clientFactory.buildUserAwareOffenderRestClient())
                .withMessage("Unable to request client-credentials grant for service call as username was not provided " +
                        "in the incoming token and username is mandatory for clientId 'mandatory-username'");

        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenClientIdIsInRequireUsernameList_andUsernameIsEmpty_whenBuild_thenThrowException() {
        when(clientDetails.getClientId()).thenReturn("mandatory-username");
        when(clientDetails.getUsername()).thenReturn("");

        assertThatExceptionOfType(UnableToGetTokenOnBehalfOfUserException.class)
                .isThrownBy(() -> clientFactory.buildUserAwareOffenderRestClient())
                .withMessage("Unable to request client-credentials grant for service call as username was not provided " +
                        "in the incoming token and username is mandatory for clientId 'mandatory-username'");

        verifyNoMoreInteractions(webClientFactory, clientDetails);
    }

    @Test
    public void givenNoUserInvolved_whenBuild_thenNotThrowAnyException() {
        clientFactory.buildUserAgnosticOffenderRestClient();
        verify(webClientFactory,times(0)).buildCommunityRestClientHelper(anyString());
        verify(clientDetails,times(0)).getClientId();
        verify(clientDetails,times(0)).getUsername();
    }

}
