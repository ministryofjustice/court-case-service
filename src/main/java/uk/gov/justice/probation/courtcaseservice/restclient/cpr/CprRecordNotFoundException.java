package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

public class CprRecordNotFoundException extends RuntimeException {

    public CprRecordNotFoundException(String identifier) {
        super("No CPR record found for identifier " + identifier);
    }
}