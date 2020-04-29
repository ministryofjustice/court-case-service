package uk.gov.justice.probation.courtcaseservice.restclient;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

class RestClientHelperTest {

    private RestClientHelper restClientHelper;

    @BeforeEach
    void beforeEach() {
        this.restClientHelper = new RestClientHelper(null, "", false);
    }

    @DisplayName("CRN error handling when we get a NOT FOUND HttpStatus")
    @Test
    void handleError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        assertThrows(OffenderNotFoundException.class, () -> restClientHelper.handleError("CRN", clientResponse).block());
    }

    @DisplayName("Error handling when the code is in the range of 400 but other than NOT FOUND")
    @Test
    void handleNonNotFoundError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.BAD_REQUEST).build();

        assertThrows(WebClientResponseException.class, () -> restClientHelper.handleError("CRN", clientResponse).block());
    }
}
