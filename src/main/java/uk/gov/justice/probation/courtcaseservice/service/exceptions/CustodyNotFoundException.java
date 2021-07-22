package uk.gov.justice.probation.courtcaseservice.service.exceptions;

public class CustodyNotFoundException extends EntityNotFoundException {
    public CustodyNotFoundException(String msg, Object... args) {
        super(msg, args);
    }
}
