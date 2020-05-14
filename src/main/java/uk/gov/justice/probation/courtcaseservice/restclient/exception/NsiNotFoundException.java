package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class NsiNotFoundException extends RestResourceNotFoundException {
    public NsiNotFoundException(Long nsiId) {
        super(String.format("Nsi with id '%s' not found", nsiId));
    }
}
