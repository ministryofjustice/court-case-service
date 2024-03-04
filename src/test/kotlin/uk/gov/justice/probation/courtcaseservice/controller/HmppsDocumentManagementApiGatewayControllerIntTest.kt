package uk.gov.justice.probation.courtcaseservice.controller

import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper
import java.io.File

@Sql(
    scripts = ["classpath:sql/before-common.sql", "classpath:case-progress.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
internal class HmppsDocumentManagementApiGatewayControllerIntTest: BaseIntTest() {

    @Autowired
    lateinit var hearingRepository: HearingRepository

    companion object {
        const val HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00"
        const val DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002"
        const val DOCUMENT_MANAGEMENT_API_UPLOAD_RESPONSE = """
            {
              "documentUuid": "8cdadcf3-b003-4116-9956-c99bd8df6a00",
              "documentType": "PIC_CASE_DOCUMENTS",
              "documentFilename": "test-upload-file.txt",
              "filename": "test-upload-file",
              "fileExtension": "txt",
              "fileSize": 48243,
              "mimeType": "txt"
            }
        """
    }

    @Test
    fun `given hearing id, defendant id and document, should upload document to document management API`() {

        WIRE_MOCK_SERVER.stubFor(
            post(urlPathMatching("/documents/PIC_CASE_UPLOADS/.*"))
            .withHeader("Content-Type", containing("multipart/form-data"))
            .withRequestBody(containing("file"))
            .withRequestBody(containing("test file upload content"))
                .willReturn(
                    aResponse().withStatus(201).withBody(DOCUMENT_MANAGEMENT_API_UPLOAD_RESPONSE)
                        .withHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

        RestAssured.given()
            .multiPart("file", File("./src/test/resources/document-upload/test-upload-file.txt"))
            .multiPart("file", File("./src/test/resources/document-upload/test-upload-file-two.txt"))
            .auth()
            .oauth2(TokenHelper.getToken())
            .contentType(ContentType.MULTIPART)
            .`when`()
            .post("/hearing/{hearingId}/defendant/{defendantId}/file", HEARING_ID, DEFENDANT_ID)
            .then()
            .statusCode(201)

        val hearing = hearingRepository.findFirstByHearingId(HEARING_ID).get()
        val caseDefendant = hearing.courtCase.getCaseDefendant(DEFENDANT_ID)
        val caseDefendantDocument = caseDefendant.get().documents;
        Assertions.assertThat(caseDefendantDocument).extracting("documentName")
            .containsExactlyInAnyOrder("test-upload-file.txt", "test-upload-file-two.txt", "test-upload-file-get.txt")

        Assertions.assertThat(caseDefendantDocument).extracting("documentId").isNotNull()

        WIRE_MOCK_SERVER.verify(postRequestedFor(urlEqualTo("/documents/PIC_CASE_UPLOADS/${caseDefendantDocument[0].documentId}")));
        WIRE_MOCK_SERVER.verify(postRequestedFor(urlEqualTo("/documents/PIC_CASE_UPLOADS/${caseDefendantDocument[1].documentId}")));
    }

    @Test
    fun `given hearing id, defendant id and document id, should download document from document management API`() {
        val documentId = "3cfd7d45-6f62-438e-ad64-ef3d911dfe38"

        WIRE_MOCK_SERVER.stubFor(
            get(urlPathEqualTo("/documents/$documentId/file"))
                .willReturn(
                    aResponse().withStatus(200)
                        .withBody("test file upload content")
                        .withHeader("Content-type", MediaType.TEXT_PLAIN_VALUE)
                        .withHeader(
                            HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"test-upload-file.txt\""
                        )
                )
        )
        RestAssured.registerParser(MediaType.TEXT_PLAIN.type, Parser.TEXT)
        val response = RestAssured.given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .`when`()
            .contentType(MediaType.TEXT_PLAIN_VALUE)
            .get(
                "/hearing/{hearingId}/defendant/{defendantId}/file/{documentId}/raw", HEARING_ID, DEFENDANT_ID,
                documentId
            )
        response
            .then()
            .statusCode(200)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-upload-file.txt\"")

        assertThat(response.body().prettyPrint(), `is`("test file upload content"))

        WIRE_MOCK_SERVER.verify(getRequestedFor(urlEqualTo("/documents/${documentId}/file")));
    }

    @Test
    fun `given hearing id, defendant id and document id, should delete document from document management API`() {
        val documentId = "3cfd7d45-6f62-438e-ad64-ef3d911dfe38"

        WIRE_MOCK_SERVER.stubFor(
            delete(urlPathEqualTo("/documents/$documentId"))
                .willReturn(
                    aResponse().withStatus(204)
                )
        )

        RestAssured.given()
            .auth()
            .oauth2(TokenHelper.getToken())
            .`when`()
            .contentType(MediaType.TEXT_PLAIN_VALUE)
            .delete(
                "/hearing/{hearingId}/defendant/{defendantId}/file/{documentId}", HEARING_ID, DEFENDANT_ID,
                documentId
            )
            .then()
            .statusCode(204)

        WIRE_MOCK_SERVER.verify(deleteRequestedFor(urlEqualTo("/documents/${documentId}")));
    }
}