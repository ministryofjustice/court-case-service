package uk.gov.justice.probation.courtcaseservice.service.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDateTime;

@Schema(description = "Offender Document Details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDocumentDetail {

    @Schema(required = true)
    private final String documentId;
    private final String documentName;
    private final String author;
    private final DocumentType type;
    private final String extendedDescription;
    private final LocalDateTime createdAt;
    public final boolean psr;

    @JsonIgnore
    private final Long parentPrimaryKeyId;

    private final KeyValue subType;
    private final ReportDocumentDates reportDocumentDates;


}

