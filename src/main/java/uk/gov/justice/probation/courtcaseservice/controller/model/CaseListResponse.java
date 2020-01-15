package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.Getter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.util.List;

@Getter
public class CaseListResponse {
    private List<CourtCaseEntity> cases;
}
