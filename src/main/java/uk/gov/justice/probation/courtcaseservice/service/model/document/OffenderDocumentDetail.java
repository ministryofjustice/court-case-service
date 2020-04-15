package uk.gov.justice.probation.courtcaseservice.service.model.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@ApiModel("Offender Document Details")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDocumentDetail {

    @ApiModelProperty(required = true)
    private final String alfrescoId;
    private final String documentName;
    private final String author;
    private final DocumentType type;
    private final String extendedDescription;
    private final LocalDateTime createdAt;

    private KeyValue subType;
    private ReportDocumentDates reportDocumentDates;
}

