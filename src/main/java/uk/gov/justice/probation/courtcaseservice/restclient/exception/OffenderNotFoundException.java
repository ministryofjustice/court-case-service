package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class OffenderNotFoundException extends RestResourceNotFoundException {
    public OffenderNotFoundException(String crn) {
        super(String.format("Offender with CRN '%s' not found", crn));
    }
}
