package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class CurrentOrderHeaderNotFoundException extends RestResourceNotFoundException {
    public CurrentOrderHeaderNotFoundException(String crn, Long convictionId, Long sentenceId) {
        super(String.format("CurrentOrderHeader with id '%s' not found for convictionId '%s' and crn '%s'", sentenceId, convictionId, crn));
    }

    public CurrentOrderHeaderNotFoundException(String message) {
        super(message);
    }
}
