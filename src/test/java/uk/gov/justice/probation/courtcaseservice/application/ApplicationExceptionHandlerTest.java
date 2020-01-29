package uk.gov.justice.probation.courtcaseservice.application;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.probation.courtcaseservice.controller.ErrorResponse;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;


public class ApplicationExceptionHandlerTest {

    public static final String THE_MESSAGE = "This is the message";
    private ApplicationExceptionHandler applicationExceptionHandler = new ApplicationExceptionHandler();

    @Test
    public void whenDuplicateEntityExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new DuplicateEntityException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST);
    }


    @Test
    public void whenConflictingInputExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new ConflictingInputException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.BAD_REQUEST);
    }


    @Test
    public void whenEntityNotFoundExceptionCaught_thenReturnAppropriateErrorResponse() {
        ResponseEntity<ErrorResponse> response = applicationExceptionHandler.handle(new EntityNotFoundException(THE_MESSAGE));

        assertGoodErrorResponse(response, HttpStatus.NOT_FOUND);
    }

    private void assertGoodErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus notFound) {
        assertThat(response.getStatusCode()).isEqualTo(notFound);

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(notFound.value());
        assertThat(errorResponse.getDeveloperMessage()).isEqualTo(THE_MESSAGE);
        assertThat(errorResponse.getUserMessage()).isEqualTo(THE_MESSAGE);
    }

}