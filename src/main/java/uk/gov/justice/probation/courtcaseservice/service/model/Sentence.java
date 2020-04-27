package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@ApiModel("Sentence")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sentence {
    private String description;
    private Integer length;
    private String lengthUnits;
    private Integer lengthInDays;
    private LocalDate terminationDate;
    private String terminationReason;

    @JsonIgnore
    private UnpaidWork unpaidWork;
}
