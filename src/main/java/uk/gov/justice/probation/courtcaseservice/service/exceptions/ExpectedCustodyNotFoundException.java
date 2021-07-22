package uk.gov.justice.probation.courtcaseservice.service.exceptions;

public class ExpectedCustodyNotFoundException extends RuntimeException {
    public ExpectedCustodyNotFoundException(String message) {
        super(message);
    }
}
