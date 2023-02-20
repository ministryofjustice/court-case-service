package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Service
@Slf4j
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

        return hearingRepository.findFirstByHearingId(hearingId)
            .map(hearingEntity -> hearingNotesRepository.save(hearingNoteEntity))
            .map(hearingNote -> {
                telemetryService.trackCreateHearingNoteEvent(TelemetryEventType.HEARING_NOTE_ADDED, hearingNote);
                return hearingNote;
            })
            .orElseThrow(() -> new EntityNotFoundException("Hearing %s not found", hearingId));
    }

    public void deleteHearingNote(String hearingId, Long noteId, String userUuid) {

        log.info("Delete request for hearingId {} / noteId {} by user {}", hearingId, noteId, userUuid);

        hearingNotesRepository.findById(noteId).ifPresentOrElse(hearingNoteEntity -> {

            validateHearingNoteUpdate(hearingId, noteId, hearingNoteEntity);

            if (!equalsIgnoreCase(hearingNoteEntity.getCreatedByUuid(), userUuid)) {
                log.warn("User {} illegal attempt to delete note {} on hearing {}", userUuid, noteId, hearingId);
                throw new ForbiddenException(String.format("User %s does not have permissions to delete note %s on hearing %s", userUuid, noteId, hearingId));
            }
            hearingNoteEntity.setDeleted(true);
            hearingNotesRepository.save(hearingNoteEntity);
            telemetryService.trackDeleteHearingNoteEvent(TelemetryEventType.HEARING_NOTE_DELETED, hearingNoteEntity);
        }, () -> throwNoteNotFound(noteId, hearingId));
    }

    public void updateHearingNote(HearingNoteEntity hearingNoteUpdate, Long noteId) {
        final var hearingId = hearingNoteUpdate.getHearingId();
        final var userUuid = hearingNoteUpdate.getCreatedByUuid();
        log.info("Update request for hearingId {} / noteId {} by user {}", hearingId, noteId, userUuid);

        hearingNotesRepository.findById(noteId).ifPresentOrElse(hearingNoteEntity -> {
            validateHearingNoteUpdate(hearingId, noteId, hearingNoteEntity);
            if (!equalsIgnoreCase(hearingNoteEntity.getCreatedByUuid(), userUuid)) {
                log.warn("User {} illegal attempt to update note {} on hearing {}", userUuid, noteId, hearingId);
                throw new ForbiddenException(String.format("User %s does not have permissions to update note %s on hearing %s", userUuid, noteId, hearingId));
            }
            hearingNoteEntity.updateNote(hearingNoteUpdate);
            hearingNotesRepository.save(hearingNoteEntity);
            telemetryService.trackUpdateHearingNoteEvent(TelemetryEventType.HEARING_NOTE_DELETED, hearingNoteEntity);
        }, () -> throwNoteNotFound(noteId, hearingId));
    }

    private static void validateHearingNoteUpdate(String hearingId, Long noteId, HearingNoteEntity hearingNoteEntity) {
        if (!equalsIgnoreCase(hearingNoteEntity.getHearingId(), hearingId)) {
            throw new ConflictingInputException(String.format("Note %d not found for hearing %s", noteId, hearingId));
        }
    }

    private static void throwNoteNotFound(Long noteId, String hearingId) {
        throw new EntityNotFoundException("Note %s not found for hearing %s", noteId, hearingId);
    }
}
