package uk.gov.justice.probation.courtcaseservice.service.exceptions;

public class BreachNotFoundException extends EntityNotFoundException {
    public BreachNotFoundException(String msg, Object... args) {
        super(msg, args);
    }
}
