package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public HearingNoteEntity createHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteEntity) {

        return createHearingNote(hearingId, defendantId, hearingNoteEntity, false);
    }

    private HearingNoteEntity createHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteEntity, boolean draft) {

        return hearingRepository.findFirstByHearingId(hearingId)
            .map(hearingEntity -> hearingEntity.getHearingDefendant(defendantId))
            .map(hearingDefendantEntity ->
                hearingDefendantEntity.getHearingNoteDraft(hearingNoteEntity.getCreatedByUuid())
                .map(dbHearingNote ->
                    {
                        dbHearingNote.updateNote(hearingNoteEntity.withDraft(draft));
                        return dbHearingNote;
                    }
                ).orElseGet(() -> hearingDefendantEntity.addHearingNote(hearingNoteEntity.withDraft(draft))))
            .map(hearingNote -> hearingNotesRepository.save(hearingNote))
            .map(hearingNote -> {
                if(!draft) {
                    telemetryService.trackCreateHearingNoteEvent(hearingNote);
                }
                return hearingNote;
            })
            .orElseThrow(() -> new EntityNotFoundException("Hearing %s not found", hearingId));
    }

    public HearingNoteEntity createOrUpdateHearingNoteDraft(String hearingId, String defendantId, HearingNoteEntity hearingNoteEntity) {

        return createHearingNote(hearingId, defendantId, hearingNoteEntity, true);
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
            telemetryService.trackDeleteHearingNoteEvent(hearingNoteEntity);
        }, () -> throwNoteNotFound(noteId, hearingId, null, userUuid));
    }

    public void deleteHearingNoteDraft(String hearingId, String userUuid) {

        log.info("Request to delete draft note on a hearingId {} by user {}", hearingId, userUuid);

        hearingNotesRepository.findByHearingIdAndCreatedByUuidAndDraftIsTrue(hearingId, userUuid).ifPresentOrElse(hearingNoteEntity -> {
            hearingNoteEntity.setDeleted(true);
            hearingNotesRepository.save(hearingNoteEntity);
        }, () -> {
            throw new EntityNotFoundException("Draft note not found for user %s on hearing %s", userUuid, hearingId);
        });
    }

    public void updateHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteUpdate, Long noteId) {
        final var userUuid = hearingNoteUpdate.getCreatedByUuid();
        log.info("Update request for hearingId {} / defendantId {} / noteId {} by user {}", hearingId, defendantId, noteId, userUuid);

        hearingRepository.findFirstByHearingId(hearingId)
            .map(hearingEntity -> hearingEntity.getHearingDefendant(defendantId))
            .flatMap(hearingDefendantEntity ->
                hearingDefendantEntity.getNotes().stream().filter(
                        hearingNoteEntity -> noteId.equals(hearingNoteEntity.getId()) && hasWritePermission(hearingNoteUpdate, hearingNoteEntity))
                    .findFirst())
            .ifPresentOrElse(hearingNoteEntity -> {
                hearingNoteEntity.updateNote(hearingNoteUpdate);
                hearingNotesRepository.save(hearingNoteEntity);
                telemetryService.trackUpdateHearingNoteEvent(hearingNoteEntity);
            }, () -> throwNoteNotFound(noteId, hearingId, defendantId, hearingNoteUpdate.getCreatedByUuid()));
    }

    private static boolean hasWritePermission(HearingNoteEntity hearingNoteUpdate, HearingNoteEntity hearingNoteEntity) {
        return StringUtils.equalsIgnoreCase(hearingNoteEntity.getCreatedByUuid(), hearingNoteUpdate.getCreatedByUuid());
    }

    private static void validateHearingNoteUpdate(String hearingId, Long noteId, HearingNoteEntity hearingNoteEntity) {
        if (!equalsIgnoreCase(hearingNoteEntity.getHearingId(), hearingId)) {
            throw new ConflictingInputException(String.format("Note %d not found for hearing %s", noteId, hearingId));
        }
    }

    private static void throwNoteNotFound(Long noteId, String hearingId, String defendantId, String createdByUuid) {
        throw new EntityNotFoundException("Note %s not found for hearing %s, defendant %s and user uuid %s", noteId, hearingId, defendantId, createdByUuid);
    }
}
