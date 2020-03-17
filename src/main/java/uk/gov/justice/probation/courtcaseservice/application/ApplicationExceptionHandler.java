package uk.gov.justice.probation.courtcaseservice.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.justice.probation.courtcaseservice.controller.ErrorResponse;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler(OffenderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(OffenderNotFoundException e) {
        log.error("OffenderNotFound: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder().status(404)
                .developerMessage(e.getMessage())
                .userMessage(e.getMessage()).build(), NOT_FOUND);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(EntityNotFoundException e) {
        log.error("EntityNotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder().status(404)
                .developerMessage(e.getMessage())
                .userMessage(e.getMessage()).build(), NOT_FOUND);
    }

    @ExceptionHandler(ConflictingInputException.class)
    public ResponseEntity<ErrorResponse> handle(ConflictingInputException e) {
        log.error("ConflictingInputException: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder().status(BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .userMessage(e.getMessage()).build(), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateEntityException e) {
        log.error("DuplicateEntityException: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder().status(BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .userMessage(e.getMessage()).build(), BAD_REQUEST);
    }

}
