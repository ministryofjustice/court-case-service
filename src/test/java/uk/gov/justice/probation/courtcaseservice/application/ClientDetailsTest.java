package uk.gov.justice.probation.courtcaseservice.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ClientDetailsTest {

    private final ClientDetails clientDetails = new ClientDetails();

    @Test
    public void getAuditorShouldReturnUsernameIfAvailable() {
        clientDetails.setClientDetails("client-id", "username");
        assertThat(clientDetails.getCurrentAuditor().orElseThrow()).isEqualTo("username(client-id)");
    }

    @Test
    public void getAuditorShouldReturnClientIdIfNoUsernameIsAvailable() {
        clientDetails.setClientDetails("client-id", null);
        assertThat(clientDetails.getCurrentAuditor().get()).isEqualTo("(client-id)");
    }

    @Test
    public void getAuditorShouldReturnEmptyIfNoUsernameOrClientIdIsAvailable() {
        clientDetails.setClientDetails(null, null);
        assertThat(clientDetails.getCurrentAuditor()).isEmpty();
    }
}