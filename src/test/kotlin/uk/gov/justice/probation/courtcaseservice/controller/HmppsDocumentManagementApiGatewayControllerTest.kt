package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.service.HmppsDocumentManagementService
import java.io.ByteArrayInputStream
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class HmppsDocumentManagementApiGatewayControllerTest {

    @Mock
    lateinit var hmppsDocumentManagementService: HmppsDocumentManagementService

    @InjectMocks
    lateinit var hmppsDocumentManagementApiGatewayController: HmppsDocumentManagementApiGatewayController

    @Mock
    lateinit var multipartFile: MultipartFile

    @Test
    fun `given hearing id, defendant id and document id to upload document should invoke document management service`() {

        hmppsDocumentManagementApiGatewayController.uploadDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, multipartFile)
        verify(hmppsDocumentManagementService).uploadDocuments(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, multipartFile)
    }

    @Test
    fun `given hearing id, defendant id and document id to download document should invoke document management service`() {

        val documentId = "document-id-one"
        val from = Flux.just(InputStreamResource
            (ByteArrayInputStream("asdfd".toByteArray())))
        given(hmppsDocumentManagementService.getDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, documentId))
            .willReturn(ResponseEntity.ok().body(from))

        hmppsDocumentManagementApiGatewayController.getDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID,
            documentId
        )
        verify(hmppsDocumentManagementService).getDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, documentId)
    }

    @Test
    fun `given hearing id, defendant id and document id to delete document should invoke document management service`() {
        val documentId = "document-id-one"
        given(hmppsDocumentManagementService.deleteDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, documentId))
            .willReturn(Optional.of(Mono.just(ResponseEntity.ok().body(""))))
        hmppsDocumentManagementApiGatewayController.deleteDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, documentId)
        verify(hmppsDocumentManagementService).deleteDocument(EntityHelper.HEARING_ID, EntityHelper.DEFENDANT_ID, documentId)
    }
}