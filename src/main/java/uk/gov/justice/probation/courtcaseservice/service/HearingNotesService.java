package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

@Service
public class HearingNotesService {

    private final HearingRepository hearingRepository;
    private final HearingNotesRepository hearingNotesRepository;
    private final TelemetryService telemetryService;

    public HearingNotesService(HearingRepository hearingRepository, HearingNotesRepository hearingNotesRepository,
                               TelemetryService telemetryService) {
        this.hearingRepository = hearingRepository;
        this.hearingNotesRepository = hearingNotesRepository;
        this.telemetryService = telemetryService;
    }

    public HearingNoteEntity createHearingNote(HearingNoteEntity hearingNoteEntity) {

        var hearingId = hearingNoteEntity.getHearingId();

        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId)
            .map(hearingEntity -> hearingNotesRepository.save(hearingNoteEntity))
            .orElseThrow(() -> new EntityNotFoundException("Hearing %s not found", hearingId));
    }

}
