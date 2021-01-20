package uk.gov.justice.probation.courtcaseservice.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.controller.ErrorResponse;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.RestResourceNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handle(MissingServletRequestParameterException e) {
        return responseEntityFrom(e, BAD_REQUEST);
    }


    @ExceptionHandler(RestResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(RestResourceNotFoundException e) {
        log.error("RestResourceNotFound: {}", e.getMessage());
        return responseEntityFrom(e, NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(EntityNotFoundException e) {
        log.error("EntityNotFoundException: {}", e.getMessage());
        return responseEntityFrom(e, NOT_FOUND);
    }

    @ExceptionHandler(ConflictingInputException.class)
    public ResponseEntity<ErrorResponse> handle(ConflictingInputException e) {
        log.error("ConflictingInputException: {}", e.getMessage());
        return responseEntityFrom(e, BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateEntityException e) {
        log.error("DuplicateEntityException: {}", e.getMessage());
        return responseEntityFrom(e, BAD_REQUEST);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handle(WebClientResponseException e) {
        log.error("Unexpected error from WebClient - Status {}, Body {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);

        return responseEntityFrom(e, e.getStatusCode());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handle(ForbiddenException e) {
        return responseEntityFrom(e, FORBIDDEN);
    }

    public ResponseEntity<ErrorResponse> responseEntityFrom(Exception exception, HttpStatus status) {
        final var response = ErrorResponse.builder()
                .status(status.value())
                .developerMessage(exception.getMessage())
                .userMessage(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
