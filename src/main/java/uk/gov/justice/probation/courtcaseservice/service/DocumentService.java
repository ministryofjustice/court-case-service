package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRestClient documentRestClient;

    @Autowired
    public DocumentService(final DocumentRestClient defaultClient) {
        this.documentRestClient = defaultClient;
    }

    public ResponseEntity<Resource> getDocument(String crn, String documentId) {
        return documentRestClient.getDocument(crn, documentId).block();
    }
}
