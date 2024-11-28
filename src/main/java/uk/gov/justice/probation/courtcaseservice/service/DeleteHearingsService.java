package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DuplicateHearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.util.List;

@Service
public class DeleteHearingsService {

    private final HearingRepositoryFacade hearingRepositoryFacade;
    private final DuplicateHearingRepository duplicateHearingRepository;

    public DeleteHearingsService(HearingRepositoryFacade hearingRepositoryFacade, DuplicateHearingRepository duplicateHearingRepository) {
        this.hearingRepositoryFacade = hearingRepositoryFacade;
        this.duplicateHearingRepository = duplicateHearingRepository;
    }

    public void deleteDuplicateHearings() {
        List<HearingCourtCaseDTO> oldestDuplicateHearings = duplicateHearingRepository.findOldestDuplicateHearings();
        oldestDuplicateHearings.forEach(hearingCourtCaseDTO -> {
            hearingRepositoryFacade.findById(hearingCourtCaseDTO.getId()).ifPresent(hearingEntity -> {
                hearingEntity.setDeleted(true);
                hearingEntity.getCourtCase().setDeleted(true);
                hearingEntity.getHearingDefendants().forEach(hearingDefendantEntity -> {
                    hearingDefendantEntity.setDeleted(true);
                    hearingDefendantEntity.getOffences().forEach(offenceEntity -> offenceEntity.setDeleted(true));
                });
                hearingEntity.getHearingDays().forEach(hearingDayEntity -> hearingDayEntity.setDeleted(true));
                hearingRepositoryFacade.save(hearingEntity);
            });
        });
    }
}