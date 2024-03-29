package uk.gov.justice.probation.courtcaseservice.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.controller.ErrorResponse;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.DocumentNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationExceptionHandlerTest {

    private static final String THE_MESSAGE = "This is the message";
    private final ApplicationExceptionHandler applicationExceptionHandler = new ApplicationExceptionHandler();

    @Test
    void whenOffenderNotFoundExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new OffenderNotFoundException("BAD_CRN"));

        assertGoodErrorResponse(response, HttpStatus.NOT_FOUND, "Offender with CRN 'BAD_CRN' not found");
    }

    @Test
    void whenConvictionNotFoundExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new ConvictionNotFoundException("CRN", 123456L));

        assertGoodErrorResponse(response, HttpStatus.NOT_FOUND, "Conviction with id '123456' for offender with CRN 'CRN' not found");
    }

    @Test
    void whenDocumentNotFoundExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new DocumentNotFoundException("abc-def", "BAD_CRN"));

        assertGoodErrorResponse(response, HttpStatus.NOT_FOUND, "Document with ID 'abc-def' not found for offender with CRN 'BAD_CRN'");
    }

    @Test
    void whenDuplicateEntityExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new DuplicateEntityException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST, THE_MESSAGE);
    }

    @Test
    void whenConflictingInputExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new ConflictingInputException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST, THE_MESSAGE);
    }

    @Test
    void whenEntityNotFoundExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new EntityNotFoundException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.NOT_FOUND, THE_MESSAGE);
    }

    @Test
    void whenWebClientResponseException() {
        final WebClientResponseException webClientException = mock(WebClientResponseException.class);
        when(webClientException.getRawStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        when(webClientException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(webClientException.getMessage()).thenReturn(THE_MESSAGE);

        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(webClientException);

        assertGoodErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, THE_MESSAGE);
    }

    @Test
    void whenForbiddenExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new ForbiddenException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.FORBIDDEN, THE_MESSAGE);
    }

    @Test
    void whenMethodArgumentNotValidException_thenReturnAppropriateErrorResponse() {
        final var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getMessage()).thenReturn(THE_MESSAGE);
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(exception);

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST, THE_MESSAGE);
    }

    @Test
    void whenHttpMessageNotReadableException_thenReturnAppropriateErrorResponse() {
        final var exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn(THE_MESSAGE);
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(exception);

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST, THE_MESSAGE);
    }

    private void assertGoodErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus httpStatus, String message) {
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(httpStatus.value());
        assertThat(errorResponse.getDeveloperMessage()).isEqualTo(message);
        assertThat(errorResponse.getUserMessage()).isEqualTo(message);
    }

}
