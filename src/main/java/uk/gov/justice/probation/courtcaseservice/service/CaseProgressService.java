package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public List<CaseProgressHearing> getCaseHearingProgress(String caseId, String defendantId) {
        return hearingRepository.findHearingsByCaseId(caseId)
            .map(hearingEntities -> hearingEntities.stream()
                    .filter(
                            hearingEntity -> hearingEntity.getHearingDefendant(defendantId) != null
                    )
                    .map(
                hearingEntity -> CaseProgressHearing.of(hearingEntity, defendantId, Optional.ofNullable(hearingEntity.getHearingDefendant(defendantId)).map(HearingDefendantEntity::getNotes).orElse(List.of()))
            ).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }
}
