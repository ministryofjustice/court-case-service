package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DuplicateHearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.util.List;

@Slf4j
@Service
public class DeleteHearingsService {

    private final HearingRepositoryFacade hearingRepositoryFacade;
    private final DuplicateHearingRepository duplicateHearingRepository;
    private final TelemetryService telemetryService;
    private final FeatureFlags featureFlags;

    public DeleteHearingsService(HearingRepositoryFacade hearingRepositoryFacade,
                                 DuplicateHearingRepository duplicateHearingRepository,
                                 TelemetryService telemetryService,
                                 FeatureFlags featureFlags) {
        this.hearingRepositoryFacade = hearingRepositoryFacade;
        this.duplicateHearingRepository = duplicateHearingRepository;
        this.telemetryService = telemetryService;
        this.featureFlags = featureFlags;
    }

    public void deleteDuplicateHearings() {
        List<HearingCourtCaseDTO> oldestDuplicateHearings = duplicateHearingRepository.findOldestDuplicateHearings();
        logHearingsToBeDeleted(oldestDuplicateHearings);
        if(featureFlags.deleteHearing()) {
            hearingRepositoryFacade.setHearingsToDeleted(oldestDuplicateHearings.stream().map(HearingCourtCaseDTO::getId).toList());
        }
    }

    private void logHearingsToBeDeleted(List<HearingCourtCaseDTO> hearingsCourtCaseDTOs) {
        hearingsCourtCaseDTOs.forEach(hearingCourtCaseDTO -> {
            if (featureFlags.deleteHearing()) {
                log.info("Soft deleting duplicate hearing with id {}, hearing id {} and case id {}", hearingCourtCaseDTO.getId(), hearingCourtCaseDTO.getHearingId(), hearingCourtCaseDTO.getCaseId());
                telemetryService.trackDeleteHearingEvent(TelemetryEventType.PIC_DELETE_HEARING, hearingCourtCaseDTO, featureFlags.deleteHearing());
            } else {
                log.info("DryRun enabled, soft deleting duplicate hearing with id {}, hearing id {} and case id {}", hearingCourtCaseDTO.getId(), hearingCourtCaseDTO.getHearingId(), hearingCourtCaseDTO.getCaseId());
                telemetryService.trackDeleteHearingEvent(TelemetryEventType.PIC_DELETE_HEARING, hearingCourtCaseDTO, featureFlags.deleteHearing());
            }
        });
    }
}