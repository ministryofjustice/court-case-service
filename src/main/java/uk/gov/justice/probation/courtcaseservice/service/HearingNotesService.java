package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

@Service
@Slf4j
public class HearingNotesService {

    private final HearingRepository hearingRepository;
    private final HearingNotesRepository hearingNotesRepository;
    private final TelemetryService telemetryService;

    private final HearingEntityInitService hearingNotesServiceInitService;

    public HearingNotesService(HearingRepository hearingRepository, HearingNotesRepository hearingNotesRepository,
                               TelemetryService telemetryService, HearingEntityInitService hearingNotesServiceInitService) {
        this.hearingRepository = hearingRepository;
        this.hearingNotesRepository = hearingNotesRepository;
        this.telemetryService = telemetryService;
        this.hearingNotesServiceInitService = hearingNotesServiceInitService;
    }

    public HearingNoteEntity createHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteEntity) {
        return createHearingNote(hearingId, defendantId, hearingNoteEntity, false);
    }

    public HearingNoteEntity createHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteEntity, boolean draft) {
        return hearingNotesServiceInitService.findFirstByHearingIdInitNoteHearingDefendant(hearingId)
            .map(hearingEntity -> hearingEntity.getHearingDefendant(defendantId))
            .map(hearingDefendantEntity ->
                hearingDefendantEntity.getHearingNoteDraft(hearingNoteEntity.getCreatedByUuid())
                .map(dbHearingNote ->
                    {
                        dbHearingNote.updateNote(hearingNoteEntity.withDraft(draft));
                        return dbHearingNote;
                    }
                ).orElseGet(() -> hearingDefendantEntity.addHearingNote(hearingNoteEntity.withDraft(draft))))
            .map(hearingNote -> {
                hearingNotesRepository.save(hearingNote);
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

    public void deleteHearingNote(String hearingId, String defendantId, Long noteId, String userUuid) {

        log.info("Delete request for hearingId {} / defendantId {} / noteId {} by user {}", hearingId, defendantId, noteId, userUuid);
        hearingNotesServiceInitService.findFirstByHearingIdInitNoteHearingDefendant(hearingId)
            .map(hearingEntity -> hearingEntity.getHearingDefendant(defendantId))
            .flatMap(hearingDefendantEntity ->
                hearingDefendantEntity.getNotes().stream().filter(
                        hearingNoteEntity -> noteId.equals(hearingNoteEntity.getId()) && hasWritePermission(HearingNoteEntity.builder().createdByUuid(userUuid).build(), hearingNoteEntity))
                    .findFirst())
            .ifPresentOrElse(hearingNoteEntity -> {
                hearingNoteEntity.setDeleted(true);
                hearingNotesRepository.save(hearingNoteEntity);
                telemetryService.trackDeleteHearingNoteEvent(hearingNoteEntity);
            }, () -> throwNoteNotFound(noteId, hearingId, defendantId, userUuid));
    }

    public void deleteHearingNoteDraft(String hearingId, String defendantId, String userUuid) {

        log.info("Request to delete draft note on a hearingId {} / defendantId {} by user {}", hearingId, defendantId, userUuid);

        hearingNotesServiceInitService.findFirstByHearingIdInitNoteHearingDefendant(hearingId)
            .map(hearingEntity -> hearingEntity.getHearingDefendant(defendantId))
            .flatMap(hearingDefendantEntity ->
                hearingDefendantEntity.getNotes().stream().filter(
                        hearingNoteEntity -> hearingNoteEntity.isDraft() && hasWritePermission(HearingNoteEntity.builder().createdByUuid(userUuid).build(), hearingNoteEntity))
                    .findFirst())
            .ifPresentOrElse(hearingNoteEntity -> {
                hearingNoteEntity.setDeleted(true);
                hearingNotesRepository.save(hearingNoteEntity);
            }, () -> {
                throw new EntityNotFoundException("Draft note not found for user %s on hearing %s / defendant %s", userUuid, hearingId, defendantId);
            });
    }

    public void updateHearingNote(String hearingId, String defendantId, HearingNoteEntity hearingNoteUpdate, Long noteId) {
        final var userUuid = hearingNoteUpdate.getCreatedByUuid();
        log.info("Update request for hearingId {} / defendantId {} / noteId {} by user {}", hearingId, defendantId, noteId, userUuid);

        hearingNotesServiceInitService.findFirstByHearingIdInitNoteHearingDefendant(hearingId)
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

    private static void throwNoteNotFound(Long noteId, String hearingId, String defendantId, String createdByUuid) {
        throw new EntityNotFoundException("Note %s not found for hearing %s, defendant %s and user uuid %s or the user does not have permissions to modify", noteId, hearingId, defendantId, createdByUuid);
    }
}
