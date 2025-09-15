package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonInclude(Include.NON_NULL)
public record HearingOutcomeNotRequired(@NotNull boolean hearingOutcomeNotRequired) {}
