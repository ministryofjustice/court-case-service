package uk.gov.justice.probation.courtcaseservice.service.model.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "Groupings of documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupedDocuments {

    @ApiModelProperty(value = "Documents relevant to the Offender generally")
    private final List<OffenderDocumentDetail> documents;
    @ApiModelProperty(value = "Documents specific relevant to each of the convictions")
    private final List<ConvictionDocuments> convictions;

}
