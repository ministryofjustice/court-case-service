package uk.gov.justice.probation.courtcaseservice.client.model.documentmanagement

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(
  description = "Document properties and metadata associated with the document. The document file must be downloaded " +
    "separately using the GET /documents/{documentUuid}/file endpoint.",
)
data class DocumentUploadResponse(
  val documentUuid: UUID,

  val documentFilename: String,

  val filename: String,

  val fileExtension: String,

  val mimeType: String,
)
