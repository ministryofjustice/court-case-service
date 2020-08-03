package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel("Sentence")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sentence {
    private final String sentenceId;
    private final String description;
    private final Integer length;
    private final String lengthUnits;
    private final Integer lengthInDays;
    private final LocalDate terminationDate;
    private final String terminationReason;

    @JsonIgnore
    private final UnpaidWork unpaidWork;
}
