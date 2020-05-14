package uk.gov.justice.probation.courtcaseservice.restclient.exception;

public class DocumentNotFoundException extends RestResourceNotFoundException {
    public DocumentNotFoundException(String documentId, String crn) {
        super(String.format("Document with ID '%s' not found for offender with CRN '%s'", documentId, crn));
    }
}
