package uk.gov.justice.probation.courtcaseservice.service.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@ApiModel("Offender Document Details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDocumentDetail {

    @ApiModelProperty(required = true)
    private final String documentId;
    private final String documentName;
    private final String author;
    private final DocumentType type;
    private final String extendedDescription;
    private final LocalDateTime createdAt;

    @JsonIgnore
    private final Long parentPrimaryKeyId;

    private final KeyValue subType;
    private final ReportDocumentDates reportDocumentDates;
}

