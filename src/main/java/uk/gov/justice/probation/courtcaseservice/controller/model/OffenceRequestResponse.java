package uk.gov.justice.probation.courtcaseservice.controller.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class OffenceRequestResponse {
    @NotBlank
    private final String offenceTitle;
    @NotNull
    private final String offenceSummary;
    private final String act;
    private final Integer listNo;
    private List<JudicialResult> judicialResults;

}
