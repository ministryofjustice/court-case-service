package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseProgressMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;

@Service
public class CaseProgressService {

    private final HearingRepository hearingRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final CaseProgressMapper caseProgressMapper;

    public CaseProgressService(HearingRepository hearingRepository, CourtCaseRepository courtCaseRepository, CaseProgressMapper caseProgressMapper) {
        this.hearingRepository = hearingRepository;
        this.courtCaseRepository = courtCaseRepository;
        this.caseProgressMapper = caseProgressMapper;
    }

    public List<CaseProgressHearing> getCaseHearingProgress(String caseId) {
        return courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
            .map(courtCaseEntity -> getHearingsByCaseId(caseId))
            .map(caseProgressMapper::mapFrom)
            .orElseThrow(() -> new EntityNotFoundException("Court case with id {} does not exist", caseId));
    }

    private List<HearingEntity> getHearingsByCaseId(String caseId) {
        List<HearingEntity> hearingsByCaseId = hearingRepository.findHearingsByCaseId(caseId);
        return hearingsByCaseId;
    }
}
