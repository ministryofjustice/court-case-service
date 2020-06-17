package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel("CurrentOrderHeaderDetail")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentOrderHeaderDetail {
    @JsonIgnore
    private Long sentenceId;
    private KeyValue custodialType;
    private KeyValue sentence;
    private KeyValue mainOffence;
    private LocalDate sentenceDate;
    private LocalDate actualReleaseDate;
    private LocalDate licenceExpiryDate;
    private LocalDate pssEndDate;
    private Integer length;
    private String lengthUnits;
}


