package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import lombok.Setter;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@ApiModel("Conviction")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Conviction {
    private String convictionId;
    private Boolean active;
    private Boolean inBreach;
    private LocalDate convictionDate;
    private List<Offence> offences;
    private Sentence sentence;
    private LocalDate endDate;
    @Setter
    private List<OffenderDocumentDetail> documents;
}
