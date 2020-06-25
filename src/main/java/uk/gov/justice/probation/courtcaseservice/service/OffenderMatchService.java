package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

@Service
public class OffenderMatchService {
    @Autowired
    private CourtCaseService courtCaseService;

    public OffenderMatchEntity createMatch(String courtCode, String caseNo, OffenderMatchRequest offenderMatchRequest) {
        CourtCaseEntity courtCase = courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
        return null;
    }
}
