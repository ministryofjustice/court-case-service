package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class CustodialStatusNotFoundException extends RestResourceNotFoundException {
    public CustodialStatusNotFoundException(String crn, Long convictionId, Long sentenceId) {
        super(String.format("CurrentOrderHeader with id '%s' not found for convictionId '%s' and crn '%s'", sentenceId, convictionId, crn));
    }

    public CustodialStatusNotFoundException(String message) {
        super(message);
    }
}
