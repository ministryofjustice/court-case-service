package uk.gov.justice.probation.courtcaseservice.controller.exceptions;

public class ConflictingInputException extends RuntimeException {
    public ConflictingInputException(String message) {
        super(message);
    }
}
