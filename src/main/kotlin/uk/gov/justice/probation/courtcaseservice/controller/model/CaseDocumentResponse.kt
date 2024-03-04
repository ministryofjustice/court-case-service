package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Hearing outcome input model")
data class CaseDocumentResponse(val id: String,
                                val datetime: LocalDateTime,
                                val file: FileResponse
) {
    data class FileResponse(val name: String, val size: Number? = 0)
}
