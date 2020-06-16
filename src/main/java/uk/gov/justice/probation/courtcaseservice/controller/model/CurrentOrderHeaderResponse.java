package uk.gov.justice.probation.courtcaseservice.controller.model;

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
    private final Long sentenceId;
    private final String sentenceDescription;
    private final KeyValue custodialType;
    private final String mainOffenceDescription;
    private final LocalDate sentenceDate;
    private final LocalDate actualReleaseDate;
    private final LocalDate licenceExpiryDate;
    private final LocalDate pssEndDate;
    private final Integer length;
    private final String lengthUnits;
}



