package uk.gov.justice.probation.courtcaseservice.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Schema(description = "Response object for a list of cases")
public class CaseListResponse {
    private final List<CourtCaseResponse> cases;
}
