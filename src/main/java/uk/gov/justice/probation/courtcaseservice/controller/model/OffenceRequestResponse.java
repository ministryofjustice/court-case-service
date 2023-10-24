package uk.gov.justice.probation.courtcaseservice.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenceRequestResponse {
    @NotBlank
    private final String offenceTitle;
    @NotNull
    private final String offenceSummary;
    private final String act;
    private final Integer listNo;
    private final String offenceCode;
    private List<JudicialResult> judicialResults;
    private Plea plea;
    private Verdict verdict;

}
