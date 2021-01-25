package uk.gov.justice.probation.courtcaseservice.security;

public class UnableToGetTokenOnBehalfOfUserException extends RuntimeException {
    public UnableToGetTokenOnBehalfOfUserException(String message) {
        super(message);
    }

    public UnableToGetTokenOnBehalfOfUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
