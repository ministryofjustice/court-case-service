package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientHelperTest {

    private RestClientHelper restClientHelper;

    @BeforeEach
    void beforeEach() {
        this.restClientHelper = new RestClientHelper(null, "", false);
    }

    @DisplayName("CRN error handling when we get a NOT FOUND HttpStatus")
    @Test
    void handleOffenderError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        assertThrows(OffenderNotFoundException.class, () -> restClientHelper.handleOffenderError("CRN", clientResponse).block());
    }

    @DisplayName("Conviction error handling when we get a NOT FOUND HttpStatus")
    @Test
    void handleConvictionError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        assertThrows(ConvictionNotFoundException.class, () -> restClientHelper.handleConvictionError("CRN", 12345L, clientResponse).block());
    }

    @DisplayName("Nsi error handling when we get a NOT FOUND HttpStatus")
    @Test
    void handleNsiError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        assertThrows(NsiNotFoundException.class, () -> restClientHelper.handleNsiError("CRN", 12345L, 123456L, clientResponse).block());
    }

    @DisplayName("Error handling when the code is in the range of 400 but other than NOT FOUND")
    @Test
    void handleNonNotFoundError() {
        final ClientResponse clientResponse = ClientResponse.create(HttpStatus.BAD_REQUEST).build();

        assertThrows(WebClientResponseException.class, () -> restClientHelper.handleOffenderError("CRN", clientResponse).block());
    }
}
