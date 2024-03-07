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
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseDocumentResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HmppsDocumentApiMetadata
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class HmppsDocumentManagementService (val hmppsDocumentManagementApiClient: HmppsDocumentManagementApiClient,
                                      val hearingRepositoryFacade: HearingRepositoryFacade,
                                      val courtCaseRepository: CourtCaseRepository,
                                      @Value("\${hmpps-document-management-api.document-type:PIC_CASE_DOCUMENTS}") val picDocumentType: String
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun deleteDocument(hearingId: String, defendantId: String, documentId: String): Optional<Mono<ResponseEntity<Any>>> {

        return getHearingEntity(hearingId)?.courtCase?.getCaseDefendant(defendantId)?.map { it.getCaseDefendantDocument(documentId) }
            ?.map {
                val caseDefendant = it.caseDefendant
                caseDefendant.deleteCaseDocument(documentId)
                courtCaseRepository.save(caseDefendant.courtCase)
                hmppsDocumentManagementApiClient.deleteDocument(documentId)
            }
            ?: throw EntityNotFoundException("Document not found /hearing/%s/defendant/%s/documents/%s", hearingId, defendantId, documentId)
    }

    fun getDocument(
        hearingId: String,
        defendantId: String,
        documentId: String
    ): Mono<ResponseEntity<Flux<InputStreamResource>>>? {

        return getHearingEntity(hearingId)?.courtCase?.getCaseDefendant(defendantId)
            ?.map { it.getCaseDefendantDocument(documentId) }
            ?.map {
                return@map hmppsDocumentManagementApiClient.getDocument(documentId)
            }?.orElseThrow {
                throw EntityNotFoundException(
                    "Document not found /hearing/%s/defendant/%s/documents/%s",
                    hearingId,
                    defendantId,
                    documentId
                )
            }
    }

    fun uploadDocuments(hearingId: String, defendantId: String, multipartFile: MultipartFile): CaseDocumentResponse {

        val builder = MultipartBodyBuilder()
        builder.part("file", multipartFile.resource)
            .contentType(MediaType.valueOf(multipartFile.contentType))
            .filename(multipartFile.originalFilename)

        return uploadDocument(hearingId, defendantId, builder, multipartFile.originalFilename)
    }

    private fun uploadDocument(
        hearingId: String,
        defendantId: String,
        filePart: MultipartBodyBuilder,
        originalFilename: String
    ): CaseDocumentResponse {

        var hearing = getHearingEntity(hearingId)

        var hearingDefendant = hearing?.getHearingDefendant(defendantId)?: throw EntityNotFoundException("Defendant %s not found for hearing %s", defendantId, hearingId)

        filePart.part("metadata", HmppsDocumentApiMetadata(hearing.courtCase.urn, defendantId))

        var documentUuid = UUID.randomUUID().toString()
        var response = hmppsDocumentManagementApiClient.createDocument(picDocumentType, documentUuid, filePart).block()
        log.debug("Response from document management API /documents/$picDocumentType/$documentUuid : $response")

        var courtCase = hearing.courtCase
        var documentEntity = courtCase.getOrCreateCaseDefendant(hearingDefendant.defendant).createDocument(documentUuid, originalFilename)
        courtCaseRepository.save(courtCase)
        return CaseDocumentResponse(documentUuid, documentEntity.created, CaseDocumentResponse.FileResponse(originalFilename))
    }

    private fun getHearingEntity(hearingId: String): HearingEntity? =
        hearingRepositoryFacade.findFirstByHearingId(hearingId)
            .getOrElse { throw EntityNotFoundException("Hearing %s not found", hearingId) }
}