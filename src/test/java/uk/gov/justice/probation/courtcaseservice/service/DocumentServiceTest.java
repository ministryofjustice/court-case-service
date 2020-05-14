package uk.gov.justice.probation.courtcaseservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final String KNOWN_CRN = "X320741";

    private static final String KNOWN_DOCUMENT_ID = "abc-def";

    @Mock
    private DocumentRestClient documentRestClient;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("Normal successful execution to fetch document")
    @Test
    void whenDocumentGet_ThenReturnDocument() {
        final ResponseEntity<Resource> responseEntity = mock(ResponseEntity.class);
        final Mono<ResponseEntity<Resource>> mono =Mono.just(responseEntity);
        when(documentRestClient.getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID)).thenReturn(mono);

        final ResponseEntity<Resource> actual = documentService.getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID);

        assertThat(responseEntity).isSameAs(actual);
        verify(documentRestClient).getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID);
    }

    @DisplayName("The rest client returns an empty Mono.")
    @Test
    void whenDocumentGetEmptyMono_ThenReturnNull() {
        when(documentRestClient.getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID)).thenReturn(Mono.empty());

        final ResponseEntity<Resource> actual = documentService.getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID);

        assertThat(actual).isNull();
        verify(documentRestClient).getDocument(KNOWN_CRN, KNOWN_DOCUMENT_ID);
    }
}
