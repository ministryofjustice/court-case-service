package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaseProgressService {

    private final HearingRepository hearingRepository;
    private final HearingNotesRepository hearingNotesRepository;

    public CaseProgressService(HearingRepository hearingRepository,
                               HearingNotesRepository hearingNotesRepository) {
        this.hearingRepository = hearingRepository;
        this.hearingNotesRepository = hearingNotesRepository;
    }

    public List<CaseProgressHearing> getCaseHearingProgress(String caseId) {
        return hearingRepository.findHearingsByCaseId(caseId)
            .map(hearingEntities -> hearingEntities.stream().map(
                hearingEntity -> CaseProgressHearing.of(hearingEntity, hearingNotesRepository.findAllByHearingIdAndDeletedFalse(hearingEntity.getHearingId()))
            ).collect(Collectors.toList()))
            .orElse(null);
    }
}
