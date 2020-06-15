package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDate;

@ApiModel(description = "Current order header detail")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentOrderHeaderResponse {
    @JsonIgnore
    private Long sentenceId;
    private String sentenceDescription;
    private KeyValue custodialType;
    private String mainOffenceDescription;
    private LocalDate sentenceDate;
    private LocalDate actualReleaseDate;
    private LocalDate licenceExpiryDate;
    private LocalDate pssEndDate;
    private Integer length;
    private String lengthUnits;
}



