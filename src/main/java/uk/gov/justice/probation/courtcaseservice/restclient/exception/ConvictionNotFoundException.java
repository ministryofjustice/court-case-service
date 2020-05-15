package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class ConvictionNotFoundException extends RestResourceNotFoundException  {
    public ConvictionNotFoundException(String crn, Long convictionId) {
        super(String.format("Conviction with id '%s' for offender with CRN '%s' not found", convictionId, crn));
    }
}
