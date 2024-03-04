package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.HmppsDocumentManagementApiClient
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class HmppsDocumentManagementService (val hmppsDocumentManagementApiClient: HmppsDocumentManagementApiClient,
                                      val hearingRepositoryFacade: HearingRepositoryFacade,
                                      val courtCaseRepository: CourtCaseRepository,
                                      @Value("\${hmpps-document-management-api.document-type:PIC_CASE_UPLOADS}") val picDocumentType: String
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun deleteDocument(hearingId: String, defendantId: String, documentId: String): Optional<Mono<ResponseEntity<Any>>> {

        return getHearingEntity(hearingId)?.courtCase?.getCaseDefendant(defendantId)?.map { it.getCaseDefendantDocument(documentId) }
            ?.map {
                hmppsDocumentManagementApiClient.deleteDocument(documentId)
            }
            ?: throw EntityNotFoundException("Document not found /hearing/%s/defendant/%s/documents/%s", hearingId, defendantId, documentId)
    }

    fun getDocument(hearingId: String, defendantId: String, documentId: String): Optional<Mono<ResponseEntity<Flux<InputStreamResource>>>> {

        return getHearingEntity(hearingId)?.courtCase?.getCaseDefendant(defendantId)?.map { it.getCaseDefendantDocument(documentId) }
            ?.map {
                val block = hmppsDocumentManagementApiClient.getDocument(documentId)
                block
            }
            ?: throw EntityNotFoundException("Document not found /hearing/%s/defendant/%s/documents/%s", hearingId, defendantId, documentId)
    }

    fun uploadDocuments(hearingId: String, defendantId: String, multipartData: List<MultipartFile>) {

        multipartData.forEach {
            val builder = MultipartBodyBuilder();
            builder.part(it.originalFilename, it.resource)
                .contentType(MediaType.valueOf(it.contentType))

            uploadDocument(hearingId, defendantId, builder, it.originalFilename)
        }
    }

    private fun uploadDocument(
        hearingId: String,
        defendantId: String,
        filePart: MultipartBodyBuilder,
        originalFilename: String
    ) {

        var hearing = getHearingEntity(hearingId)

        var hearingDefendant = hearing?.getHearingDefendant(defendantId)?: throw EntityNotFoundException("Defendant %s not found for hearing %s", defendantId, hearingId)

        var documentUuid = UUID.randomUUID().toString()
        var response = hmppsDocumentManagementApiClient.createDocument("PIC_CASE_UPLOADS", documentUuid, filePart).block()
        log.debug("Response from document management API /documents/$picDocumentType/$documentUuid : $response")

        var courtCase = hearing.courtCase
        courtCase.getOrCreateCaseDefendant(hearingDefendant.defendant).createDocument(documentUuid, originalFilename)
        courtCaseRepository.save(courtCase)
    }

    private fun getHearingEntity(hearingId: String): HearingEntity? =
        hearingRepositoryFacade.findFirstByHearingId(hearingId)
            .getOrElse { throw EntityNotFoundException("Hearing %s not found", hearingId) }
}