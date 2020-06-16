package uk.gov.justice.probation.courtcaseservice.service.model.document;

import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "Collects documents by a conviction ID")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ConvictionDocuments {
    private final String convictionId;
    private final List<OffenderDocumentDetail> documents;
}
