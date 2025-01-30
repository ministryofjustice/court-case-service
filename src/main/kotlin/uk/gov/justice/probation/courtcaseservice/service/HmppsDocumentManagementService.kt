package uk.gov.justice.probation.courtcaseservice.service

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
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
import uk.gov.justice.probation.courtcaseservice.service.exceptions.UnsupportedFileTypeException
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class HmppsDocumentManagementService (val hmppsDocumentManagementApiClient: HmppsDocumentManagementApiClient,
                                      val hearingRepositoryFacade: HearingRepositoryFacade,
                                      val courtCaseRepository: CourtCaseRepository,
                                      @Value("\${hmpps-document-management-api.document-type:PIC_CASE_DOCUMENTS}") val picDocumentType: String,
                                      @Value("\${hmpps-document-management-api.allowed-file-extensions}") var allowedExtensions: List<String>
) {

    val picDocumentAllowedFileExtensions: List<String> = allowedExtensions.map { it.lowercase() }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun deleteDocument(hearingId: String, defendantId: String, documentId: String): Optional<Mono<ResponseEntity<Any>>> {

        return getHearingEntity(hearingId, defendantId)?.courtCase?.getCaseDefendant(defendantId)?.map { it.getCaseDefendantDocument(documentId) }
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
    ): ResponseEntity<Flux<InputStreamResource>>? {

        return getHearingEntity(hearingId, defendantId)?.courtCase?.getCaseDefendant(defendantId)
            ?.map { it.getCaseDefendantDocument(documentId) }
            ?.map {
                return@map hmppsDocumentManagementApiClient.getDocument(documentId).block()
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

        validateFileExtension(multipartFile)
        val builder = MultipartBodyBuilder()
        builder.part("file", multipartFile.resource)
            .contentType(MediaType.valueOf(multipartFile.contentType))
            .filename(multipartFile.originalFilename)

        return uploadDocument(hearingId, defendantId, builder, multipartFile.originalFilename)
    }

    private fun validateFileExtension(multipartFile: MultipartFile) {
        val extension = FilenameUtils.getExtension(multipartFile.originalFilename)?.lowercase()
        if (StringUtils.isBlank(extension) || !picDocumentAllowedFileExtensions.contains(extension)
        ) throw UnsupportedFileTypeException(
            extension,
            picDocumentAllowedFileExtensions
        )
    }

    private fun uploadDocument(
        hearingId: String,
        defendantId: String,
        filePart: MultipartBodyBuilder,
        originalFilename: String
    ): CaseDocumentResponse {

        var hearing = getHearingEntity(hearingId, defendantId)

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

    private fun getHearingEntity(hearingId: String, defendantId: String): HearingEntity? =
        hearingRepositoryFacade.findFirstByHearingIdFileUpload(hearingId, defendantId)
            .getOrElse { throw EntityNotFoundException("Hearing %s not found", hearingId) }
}