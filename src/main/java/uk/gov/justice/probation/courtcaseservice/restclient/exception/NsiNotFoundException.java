package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class NsiNotFoundException extends RestResourceNotFoundException {
    public NsiNotFoundException(String crn, Long convictionId, Long nsiId) {
        super(String.format("Nsi with id '%s' not found for convictionId '%s' and crn '%s'", nsiId, convictionId, crn));
    }
}
