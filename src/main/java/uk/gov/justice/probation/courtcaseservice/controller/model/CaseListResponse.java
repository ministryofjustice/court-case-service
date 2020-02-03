package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.Constants;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CaseListResponse {
    private List<CourtCaseEntity> cases;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    public LocalDateTime getLastUpdated() {
        return cases.stream()
                .max(Comparator.comparing(CourtCaseEntity::getLastUpdated))
                .map(CourtCaseEntity::getLastUpdated)
                .orElse(null);
    }
}
