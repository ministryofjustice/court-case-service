package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
