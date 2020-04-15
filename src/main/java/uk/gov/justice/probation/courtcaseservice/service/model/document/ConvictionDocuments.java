package uk.gov.justice.probation.courtcaseservice.service.model.document;

import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@ApiModel(value = "Collects documents by a conviction ID")
@Data
@Builder
@AllArgsConstructor
public class ConvictionDocuments {
    private String convictionId;
    private List<OffenderDocumentDetail> documents;
}
