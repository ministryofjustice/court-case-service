package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseProgressMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;

@Service
public class CaseProgressService {

    private final HearingRepository hearingRepository;
    private final CaseProgressMapper caseProgressMapper;

    public CaseProgressService(HearingRepository hearingRepository, CourtCaseRepository courtCaseRepository, CaseProgressMapper caseProgressMapper) {
        this.hearingRepository = hearingRepository;
        this.caseProgressMapper = caseProgressMapper;
    }

    public List<CaseProgressHearing> getCaseHearingProgress(String caseId) {
        return hearingRepository.findHearingsByCaseId(caseId)
            .map(caseProgressMapper::mapFrom).orElse(null);
    }
}
