package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderMatchRepository;

@Service
@AllArgsConstructor
public class OffenderMatchService {
    @Autowired
    private CourtCaseService courtCaseService;

    @Autowired
    private OffenderMatchRepository offenderMatchRepository;

    public OffenderMatchEntity createGroupedMatches(String courtCode, String caseNo, GroupedOffenderMatchesRequest offenderMatchRequest) {
//        CourtCaseEntity courtCase = courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
        return null;
    }
}
