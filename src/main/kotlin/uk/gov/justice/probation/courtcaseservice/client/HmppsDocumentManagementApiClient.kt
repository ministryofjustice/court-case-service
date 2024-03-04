package uk.gov.justice.probation.courtcaseservice.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalService
import uk.gov.justice.probation.courtcaseservice.client.model.documentmanagement.DocumentUploadResponse
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper
import java.io.ByteArrayInputStream


@Component
class HmppsDocumentManagementApiClient(
    @Qualifier("hmppsDocumentManagementApiRestClient") val clientHelper: RestClientHelper,
    @Value("\${hmpps-document-management-api.create-document}") val hmppsDocumentManagementApiCreateDocument: String,
    @Value("\${hmpps-document-management-api.document-by-uuid}") val hmppsDocumentManagementApiDocumentByUuid: String,
    @Value("\${hmpps-document-management-api.client-service-name}") val clientServiceName: String) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
        val DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER = "Service-Name"
    }

    fun createDocument(documentType: String, documentUuid: String, multipartBodyBuilder: MultipartBodyBuilder): Mono<DocumentUploadResponse> {
        log.debug("Uploading document type $documentType with UUID $documentUuid")

        val documentPath = String.format(hmppsDocumentManagementApiCreateDocument, documentType, documentUuid)
        return clientHelper
            .post(documentPath)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, clientServiceName)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                log.error("Document upload failed $documentPath with status ${it.statusCode()}")
                handle4xxError(it, HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API)
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                log.error("Document uploaded failed to $documentPath with status ${it.statusCode()}")
                handle5xxError("${it.statusCode()} error uploading document",
                    HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API)
            }
            .bodyToMono(DocumentUploadResponse::class.java)
            .doOnSuccess {
                log.info("Document upload success {}", it)
            }
    }

    fun getDocument(documentUuid: String): ResponseEntity<Flux<ByteArrayInputStream>>? {

        val documentPath = "${String.format(hmppsDocumentManagementApiDocumentByUuid, documentUuid)}/file"
        log.debug("Fetching document $documentPath")

        val partReader = DefaultPartHttpMessageReader()
        partReader.setStreaming(false)

        return clientHelper
            .get(documentPath)
            .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, clientServiceName)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                log.error("Document download failed $documentPath with status ${it.statusCode()}")
                handle4xxError(it, HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API)
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                log.error("Document download failed $documentPath with status ${it.statusCode()}")
                handle5xxError(
                    "${it.statusCode()} error downloading document",
                    HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API
                )
            }
            .toEntityFlux(ByteArrayInputStream::class.java)
            .doOnSuccess {
                log.info("Document download success {}", documentPath)
            }
            .block()
    }

    fun deleteDocument(documentUuid: String): Mono<ResponseEntity<Any>> {

        val documentPath = String.format(hmppsDocumentManagementApiDocumentByUuid, documentUuid)
        log.debug("Deleting document $documentPath")

        return clientHelper
            .delete(documentPath)
            .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, clientServiceName)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                log.error("Document delete failed $documentPath with status ${it.statusCode()}")
                handle4xxError(it, HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API)
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                log.error("Document delete failed $documentPath with status ${it.statusCode()}")
                handle5xxError(
                    "${it.statusCode()} error downloading document",
                    HttpMethod.POST, documentPath, ExternalService.DOCUMENT_MANAGEMENT_API
                )
            }.toEntity<Any>()
            .doOnSuccess {
                log.info("Document delete success {}", documentPath)
            }
    }

}