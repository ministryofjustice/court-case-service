package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.util.List;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@ApiModel(description = "Response object for a list of cases")
public class CaseListResponse {
    private final List<CourtCaseResponse> cases;
}
