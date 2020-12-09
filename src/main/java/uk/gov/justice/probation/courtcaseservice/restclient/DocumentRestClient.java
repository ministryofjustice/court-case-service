package uk.gov.justice.probation.courtcaseservice.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.DocumentMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiGroupedDocumentsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.DocumentNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class DocumentRestClient {

    @Value("${community-api.grouped-documents-by-crn-url-template}")
    private String groupedDocumentsUrlTemplate;
    @Value("${community-api.offender-document-by-crn-url-template}")
    private String offenderDocumentUrlTemplate;
    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    @Qualifier("documentApiClient")
    private RestClientHelper clientHelper;

    public Mono<GroupedDocuments> getDocumentsByCrn(String crn) {
        return clientHelper.get(String.format(groupedDocumentsUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiGroupedDocumentsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving grouped document data for CRN '%s'", crn), e))
            .map(documentsResponse -> documentMapper.documentsFrom(documentsResponse));
    }

    public Mono<ResponseEntity<Resource>> getDocument(String crn, String documentId)  {

        // We have to use exchange to get the header values which we need to forward.
        // Unlike retrieve(), exchange() does not throw exceptions which means we have to handle our own
        final Mono<ResponseEntity<Resource>> resourceMono = clientHelper.get(String.format(offenderDocumentUrlTemplate, crn, documentId), MediaType.ALL)
            .exchange()
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender document ID '%s' for CRN '%s'", documentId, crn), e))
            .flatMap(clientResponse -> clientResponse.toEntity(Resource.class));

        // This puts work off to another thread and therefore won't block
        final ResponseEntity<Resource> resp = resourceMono.toFuture().join();
        handleError(resp, crn, documentId);
        return Mono.just(resp);
    }

    private void handleError(ResponseEntity<Resource> responseEntity, String crn, String documentId) {
        final HttpStatus httpStatus = responseEntity.getStatusCode();
        if (!httpStatus.is2xxSuccessful()) {
            log.error("Received HttpStatus {} for document request. Document ID: [{}],CRN: [{}]", httpStatus.value(), documentId, crn);
            if (httpStatus.is4xxClientError()) {
                throw new DocumentNotFoundException(documentId, crn);
            }
            throw WebClientResponseException.create(httpStatus.value(),
                httpStatus.name(),
                responseEntity.getHeaders(),
                getBodyAsBytes(responseEntity.getBody()),
                StandardCharsets.UTF_8);
        }
    }

    private byte[] getBodyAsBytes(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()){
            return inputStream.readAllBytes();
        } catch (IOException e) {
            return new byte[0];
        }
    }

}
