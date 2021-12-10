package uk.gov.justice.probation.courtcaseservice.service.model.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description =  "Collects documents by a conviction ID")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ConvictionDocuments {
    private final String convictionId;
    private final List<OffenderDocumentDetail> documents;
}
