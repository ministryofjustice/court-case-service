package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class RestResourceNotFoundException extends RuntimeException {
    public RestResourceNotFoundException(String message) {
        super(message);
    }
}
