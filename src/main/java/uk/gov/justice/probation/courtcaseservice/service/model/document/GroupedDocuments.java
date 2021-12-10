package uk.gov.justice.probation.courtcaseservice.service.model.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description =  "Groupings of documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupedDocuments {

    @Schema(description = "Documents relevant to the Offender generally")
    private final List<OffenderDocumentDetail> documents;
    @Schema(description = "Documents specific relevant to each of the convictions")
    private final List<ConvictionDocuments> convictions;

}
