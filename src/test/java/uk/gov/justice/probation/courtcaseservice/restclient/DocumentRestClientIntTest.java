package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.DocumentNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentRestClientIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String SERVER_ERROR_CRN = "X320500";

    @Autowired
    private DocumentRestClient restClient;

    @Test
    void whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        Optional<GroupedDocuments> documentsResponse = restClient.getDocumentsByCrn(CRN).blockOptional();

        final GroupedDocuments groupedDocuments = documentsResponse.get();

        assertThat(groupedDocuments.getConvictions()).hasSize(2);
        assertThat(groupedDocuments.getDocuments()).hasSize(7);
    }

    @Test
    void givenKnownCrnNoDocuments_whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        GroupedDocuments documentsResponse = restClient.getDocumentsByCrn("CRN800").blockOptional().get();

        assertThat(documentsResponse.getDocuments()).isEmpty();
        assertThat(documentsResponse.getConvictions()).isEmpty();
    }

    @Test
    void givenOffenderDoesNotExist_whenGetConvictionsDocumentsByCrnCalled_ThrowException() {
        assertThrows(OffenderNotFoundException.class, () ->
            restClient.getDocumentsByCrn("CRNXXX").block()
        );
    }

    @Test
    void whenGetDocument_thenReturnDocument() throws IOException {
        final ResponseEntity<Resource> responseEntity = restClient.getDocument(CRN, "abc-def").blockOptional().get();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().contentLength()).isEqualTo(20992);
    }

    @Test
    void givenDocumentDoesNotExist_whenGetDocument_thenThrowException()  {
        assertThrows(DocumentNotFoundException.class, () ->
            restClient.getDocument(CRN, "xxx").blockOptional().get()
        );
    }

    @Test
    void whenGetDocument_ServerError_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            restClient.getDocument(SERVER_ERROR_CRN, "abc-def").blockOptional()
        );
    }
}
