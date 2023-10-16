package uk.gov.justice.probation.courtcaseservice.controller.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class GroupedOffenderMatchesRequest {
    @NotNull
    @JsonProperty("matches")
    @Valid
    private final List<OffenderMatchRequest> matches;
}
