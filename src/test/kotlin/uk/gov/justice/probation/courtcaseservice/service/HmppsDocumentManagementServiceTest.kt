package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.HmppsDocumentManagementApiClient
import uk.gov.justice.probation.courtcaseservice.client.model.documentmanagement.DocumentUploadResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_NO
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_URN
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SOURCE
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.service.exceptions.UnsupportedFileTypeException
import java.io.ByteArrayInputStream
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class HmppsDocumentManagementServiceTest {
  companion object {
    const val DOCUMENT_ID = "test-document-id-one"
    const val FILE_NAME = "test-document-id-one"
  }

  @Mock
  lateinit var hmppsDocumentManagementApiClient: HmppsDocumentManagementApiClient

  @Mock
  lateinit var hearingRepositoryFacade: HearingRepositoryFacade

  @Mock
  lateinit var courtCaseRepository: CourtCaseRepository

  lateinit var hmppsDocumentManagementService: HmppsDocumentManagementService

  @BeforeEach
  fun beforeAll() {
    hmppsDocumentManagementService = HmppsDocumentManagementService(
      hmppsDocumentManagementApiClient,
      hearingRepositoryFacade,
      courtCaseRepository,
      "PIC_DOCUMENT_UPLOADS",
      listOf("txt", "pdf"),
    )
  }

  @Test
  fun `given hearingId and defendantId should invoke document api to upload document and save document entity to DB`() {
    val fileName = "test-file.pdf"
    var multipartFile = MockMultipartFile(
      "file",
      fileName,
      MediaType.TEXT_PLAIN_VALUE,
      "Test file content".toByteArray(),
    )

    var hearing = aHearingEntity()
    hearing.courtCase = CourtCaseEntity.builder()
      .caseId(hearing.caseId)
      .caseNo(CASE_NO)
      .sourceType(SOURCE)
      .urn(CASE_URN)
      .hearings(ArrayList<HearingEntity>())
      .build()
    given(hearingRepositoryFacade.findFirstByHearingIdFileUpload(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing))

    val courtCaseEntityKArgumentCapture = argumentCaptor<CourtCaseEntity>()
    given(courtCaseRepository.save(courtCaseEntityKArgumentCapture.capture())).willReturn(CourtCaseEntity.builder().build())

    val multipartBodyBuilderKArgCaptor = argumentCaptor<MultipartBodyBuilder>()

    val defaultUuid = UUID.randomUUID()

    given(
      hmppsDocumentManagementApiClient.createDocument(
        eq("PIC_DOCUMENT_UPLOADS"),
        eq(defaultUuid.toString()),
        multipartBodyBuilderKArgCaptor.capture(),
      ),
    ).willReturn(
      Mono.just(DocumentUploadResponse(defaultUuid, FILE_NAME, FILE_NAME, "pdf", APPLICATION_PDF_VALUE)),
    )

    Mockito.mockStatic(UUID::class.java).use {
      it.`when`<Any> { UUID.randomUUID() }.thenReturn(defaultUuid)

      val resp = hmppsDocumentManagementService.uploadDocuments(HEARING_ID, DEFENDANT_ID, multipartFile)

      verify(hearingRepositoryFacade).findFirstByHearingIdFileUpload(HEARING_ID, DEFENDANT_ID)

      Assertions.assertThat(resp.file.name).isEqualTo(fileName)
      Assertions.assertThat(resp.id).isEqualTo(defaultUuid.toString())

      val actualCaseDefendantDocument = courtCaseEntityKArgumentCapture.firstValue.caseDefendants[0].documents[0]
      Assertions.assertThat(actualCaseDefendantDocument.documentId).isEqualTo(defaultUuid.toString())

      Assertions.assertThat(resp.datetime).isEqualTo(actualCaseDefendantDocument.created)

      val actualMultiPart = multipartBodyBuilderKArgCaptor.firstValue.build()

      Assertions.assertThat(actualMultiPart["file"]?.get(0)).isNotNull
      Assertions.assertThat(actualMultiPart["metadata"]?.get(0)).isNotNull
      Assertions.assertThat(actualMultiPart["metadata"]?.get(0)?.body.toString().contains("caseUrn=$CASE_URN")).isTrue()
    }
  }

  @Test
  fun `given hearing id and defendantId and defendantId not found, when upload document, should throw entity not found exception`() {
    given(hearingRepositoryFacade.findFirstByHearingIdFileUpload(HEARING_ID, "invalid-defendant-id")).willReturn(Optional.of(aHearingEntity()))

    val e = assertThrows(EntityNotFoundException::class.java) {
      hmppsDocumentManagementService.uploadDocuments(
        HEARING_ID,
        "invalid-defendant-id",
        MockMultipartFile("file", "test-file-name.txt", MediaType.TEXT_PLAIN_VALUE, "test content".toByteArray()),
      )
    }
    assertThat(e.message).isEqualTo("Defendant invalid-defendant-id not found for hearing 75e63d6c-5487-4244-a5bc-7cf8a38992db")
  }

  @Test
  fun `given hearingId, defendantId and documentId, when get document, should retrieve document from document management api`() {
    val aHearingEntity = aHearingEntity()

    val courtCaseEntity = aHearingEntity.courtCase
    val caseDefendant = courtCaseEntity.addCaseDefendant(aDefendantEntity())
    caseDefendant.createDocument(DOCUMENT_ID, FILE_NAME)

    given(hearingRepositoryFacade.findFirstByHearingIdFileUpload(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(aHearingEntity))
    given(hmppsDocumentManagementApiClient.getDocument(DOCUMENT_ID)).willReturn(
      Mono.just(ResponseEntity.ok().body(Flux.just(InputStreamResource(ByteArrayInputStream("".toByteArray()))))),
    )
    hmppsDocumentManagementService.getDocument(HEARING_ID, DEFENDANT_ID, DOCUMENT_ID)

    verify(hmppsDocumentManagementApiClient).getDocument(DOCUMENT_ID)
  }

  @Test
  fun `given hearingId, defendantId and documentId do not exist, when get document, should throw entity not found exception`() {
    val aHearingEntity = aHearingEntity()

    val courtCaseEntity = aHearingEntity.courtCase
    val caseDefendant = courtCaseEntity.addCaseDefendant(aDefendantEntity())
    caseDefendant.createDocument(DOCUMENT_ID, FILE_NAME)

    given(hearingRepositoryFacade.findFirstByHearingIdFileUpload(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(aHearingEntity))

    var e = assertThrows(EntityNotFoundException::class.java) {
      hmppsDocumentManagementService.getDocument(HEARING_ID, DEFENDANT_ID, "invalid-document-id")
    }

    Assertions.assertThat(e.message).isEqualTo("Document not found /hearing/75e63d6c-5487-4244-a5bc-7cf8a38992db/defendant/d1eefed2-04df-11ec-b2d8-0242ac130002/documents/invalid-document-id")
  }

  @Test
  fun `given hearingId, defendantId and document with invalid extension, when upload document, should throw unsupported file type exception`() {
    var e = assertThrows(UnsupportedFileTypeException::class.java) {
      hmppsDocumentManagementService.uploadDocuments(
        HEARING_ID,
        DEFENDANT_ID,
        MockMultipartFile(
          "file",
          "file-name.abc",
          MediaType.TEXT_PLAIN_VALUE,
          "Test file content".toByteArray(),
        ),
      )
    }

    Assertions.assertThat(e.message).isEqualTo("Unsupported or missing file type {abc}. Supported file types [txt, pdf]")
  }

  @Test
  fun `given hearingId, defendantId and document with no file extension, when upload document, should throw unsupported file type exception`() {
    var e = assertThrows(UnsupportedFileTypeException::class.java) {
      hmppsDocumentManagementService.uploadDocuments(
        HEARING_ID,
        DEFENDANT_ID,
        MockMultipartFile(
          "file",
          "file-name",
          MediaType.TEXT_PLAIN_VALUE,
          "Test file content".toByteArray(),
        ),
      )
    }

    Assertions.assertThat(e.message).isEqualTo("Unsupported or missing file type {}. Supported file types [txt, pdf]")
  }
}
