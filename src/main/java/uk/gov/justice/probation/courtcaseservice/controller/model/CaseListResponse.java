package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.Constants;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Response object for a list of cases")
public class CaseListResponse {
    private List<CourtCaseResponse> cases;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    public LocalDateTime getLastUpdated() {
        return cases.stream()
                .max(Comparator.comparing(CourtCaseResponse::getLastUpdated))
                .map(CourtCaseResponse::getLastUpdated)
                .orElse(null);
    }
}
