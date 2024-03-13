package uk.gov.justice.probation.courtcaseservice.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import uk.gov.justice.probation.courtcaseservice.service.exceptions.UnsupportedFileTypeException;

import static java.util.Objects.requireNonNull;
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
        log.error("Unexpected error from WebClient - Status {}, Body {}", e.getStatusCode(), e.getResponseBodyAsString(), e);

        return responseEntityFrom(e, requireNonNull(HttpStatus.resolve(e.getStatusCode().value())));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handle(ForbiddenException e) {
        return responseEntityFrom(e, FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
        return responseEntityFrom(e, BAD_REQUEST);
    }

    public ResponseEntity<ErrorResponse> responseEntityFrom(Exception exception, HttpStatus status) {
        final var response = ErrorResponse.builder()
                .status(status.value())
                .developerMessage(exception.getMessage())
                .userMessage(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageConversionException exception) {
        return responseEntityFrom(exception, BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponse> handle(UnsupportedFileTypeException exception) {
        return responseEntityFrom(exception, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}
