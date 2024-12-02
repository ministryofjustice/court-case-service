package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${feature.flags.delete-hearing:false}")
    private boolean deleteHearing;

    public DeleteHearingsService(HearingRepositoryFacade hearingRepositoryFacade, DuplicateHearingRepository duplicateHearingRepository, TelemetryService telemetryService) {
        this.hearingRepositoryFacade = hearingRepositoryFacade;
        this.duplicateHearingRepository = duplicateHearingRepository;
        this.telemetryService = telemetryService;
    }

    @Transactional
    public void deleteDuplicateHearings() {
        List<HearingCourtCaseDTO> oldestDuplicateHearings = duplicateHearingRepository.findOldestDuplicateHearings();
        oldestDuplicateHearings.forEach(hearingCourtCaseDTO -> {
            if (deleteHearing) {
                hearingRepositoryFacade.deleteHearing(hearingCourtCaseDTO.getId());
                log.info("Soft deleted duplicate hearing with id {}, hearing id {} and case id {}", hearingCourtCaseDTO.getId(), hearingCourtCaseDTO.getHearingId(), hearingCourtCaseDTO.getCaseId());
                telemetryService.trackDeleteHearingEvent(TelemetryEventType.PIC_DELETE_HEARING, hearingCourtCaseDTO, deleteHearing);
            } else {
                log.info("DryRun enabled, soft deleting duplicate hearing with id {}, hearing id {} and case id {}", hearingCourtCaseDTO.getId(), hearingCourtCaseDTO.getHearingId(), hearingCourtCaseDTO.getCaseId());
                telemetryService.trackDeleteHearingEvent(TelemetryEventType.PIC_DELETE_HEARING, hearingCourtCaseDTO, deleteHearing);
            }
        });
    }
}