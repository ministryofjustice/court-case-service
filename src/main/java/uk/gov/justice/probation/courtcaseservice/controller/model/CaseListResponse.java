package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CaseListResponse {
    private List<CourtCaseEntity> cases;
}
