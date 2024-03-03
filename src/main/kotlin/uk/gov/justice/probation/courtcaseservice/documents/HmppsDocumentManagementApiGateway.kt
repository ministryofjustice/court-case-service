package uk.gov.justice.probation.courtcaseservice.documents

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import java.util.*

@RestController
class HmppsDocumentManagementApiGateway(val hmppsDocumentManagementService: HmppsDocumentManagementService) {

    @Operation(description = "Uploads a document to HMPPS document management service")
    @PostMapping(
        value = ["/hearing/{hearingId}/defendant/{defendantId}/documents"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseStatus(CREATED)
    fun uploadDocument(
        @PathVariable("hearingId") hearingId: String,
        @PathVariable("defendantId") defendantId: String,
        @RequestParam("file") files: List<MultipartFile>
    ) {
        hmppsDocumentManagementService.uploadDocuments(hearingId, defendantId, files)
    }

    @Operation(description = "Retrieves a document with given documentId from HMPPS document manages service.")
    @GetMapping(
        value = ["/hearing/{hearingId}/defendant/{defendantId}/documents/{documentId}/raw"]
    )
    fun getDocument(
        @PathVariable("hearingId") hearingId: String,
        @PathVariable("defendantId") defendantId: String,
        @PathVariable("documentId") documentId: String
    ): ResponseEntity<ByteArray> {
        val documentResponse = hmppsDocumentManagementService.getDocument(hearingId, defendantId, documentId).get()

        return ResponseEntity.ok()
            .contentType(documentResponse.headers.contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, documentResponse.headers[HttpHeaders.CONTENT_DISPOSITION]?.get(0))
            .body(documentResponse.body.blockFirst())
    }

    @Operation(description = "Deletes a document with given documentId from HMPPS document manages service.")
    @DeleteMapping(
        value = ["/hearing/{hearingId}/defendant/{defendantId}/documents/{documentId}"]
    )
    fun deleteDocument(
        @PathVariable("hearingId") hearingId: String,
        @PathVariable("defendantId") defendantId: String,
        @PathVariable("documentId") documentId: String
    ): Mono<ResponseEntity<Any>> {
        return hmppsDocumentManagementService.deleteDocument(hearingId, defendantId, documentId).get()
    }
}