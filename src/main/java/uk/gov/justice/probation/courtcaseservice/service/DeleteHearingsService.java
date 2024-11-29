package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DuplicateHearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.util.List;

@Slf4j
@Service
public class DeleteHearingsService {

    private final HearingRepositoryFacade hearingRepositoryFacade;
    private final DuplicateHearingRepository duplicateHearingRepository;

    public DeleteHearingsService(HearingRepositoryFacade hearingRepositoryFacade, DuplicateHearingRepository duplicateHearingRepository) {
        this.hearingRepositoryFacade = hearingRepositoryFacade;
        this.duplicateHearingRepository = duplicateHearingRepository;
    }

    @Transactional
    public void deleteDuplicateHearings() {
        List<HearingCourtCaseDTO> oldestDuplicateHearings = duplicateHearingRepository.findOldestDuplicateHearings();
        oldestDuplicateHearings.forEach(hearingCourtCaseDTO -> {
            hearingRepositoryFacade.deleteHearing(hearingCourtCaseDTO.getId());
            log.info("Deleted hearing with id {}", hearingCourtCaseDTO.getId());
        });
    }
}