package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class HearingNotesServiceTest {

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private HearingNotesRepository hearingNotesRepository;
    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private HearingNotesService hearingNotesService;

    private static final String testHearingId = "test-hearing-id";
    private static final String createdByUuid = "test-user-uuid";
    private static final HearingNoteEntity hearingNote = HearingNoteEntity.builder().hearingId(testHearingId).author("test author").createdByUuid(createdByUuid).build();


    @Test
    void givenHearingIdExistsInDatabase_shouldCreateAHearingNote() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().hearingId("hearingId").build();

        given(hearingRepository.findFirstByHearingId("hearingId")).willReturn(Optional.of(HearingEntity.builder().build()));
        given(hearingNotesRepository.save(hearingNoteEntity)).willReturn(hearingNoteEntity);


        hearingNotesService.createHearingNote(hearingNoteEntity);

        verify(hearingRepository).findFirstByHearingId("hearingId");
        verify(hearingNotesRepository).save(hearingNoteEntity);
        verify(telemetryService).trackCreateHearingNoteEvent(TelemetryEventType.HEARING_NOTE_ADDED, hearingNoteEntity);
    }

    @Test
    void givenHearingIdDoesNotExistsInDatabase_shouldThrowEntityNotFound() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().hearingId("hearingId").build();

        given(hearingRepository.findFirstByHearingId("hearingId")).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            hearingNotesService.createHearingNote(hearingNoteEntity);
        }, "Hearing hearingId not found");

        verify(hearingRepository).findFirstByHearingId("hearingId");
        verifyNoInteractions(hearingNotesRepository);
    }

    @Test
    void givenHearingIdAndNoteId_shouldMarkNoteAsDeleted() {
        var noteId = 1234L;
        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(hearingNote.withId(noteId)));

        hearingNotesService.deleteHearingNote(testHearingId, noteId, createdByUuid);

        verify(hearingNotesRepository).findById(noteId);
        var expected = hearingNote.withId(noteId);
        expected.setDeleted(true);
        verify(hearingNotesRepository).save(expected);
        verify(telemetryService).trackDeleteHearingNoteEvent(expected);
    }

    @Test
    void givenHearingIdAndNoteId_andHearingIdDoesNotMatchNoteHearingId_shouldThrowComflictingInput() {
        var noteId = 1234L;
        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(hearingNote.withId(noteId)));

        var invalidHearingId = "invalid-hearing-id";
        assertThrows(ConflictingInputException.class, () -> hearingNotesService.deleteHearingNote(invalidHearingId, noteId, createdByUuid),
            "Note 1234 not found for hearing invalid-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verifyNoMoreInteractions(hearingNotesRepository);
    }

    @Test
    void givenHearingIdNoteIdAndUserUuid_andUserUuidDoesNotMatchNoteUserUuid_shouldThrowForbiddenException() {
        var noteId = 1234L;
        String invalidCreatedByUuid = "invalid-user-uuid";
        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(hearingNote.withId(noteId)));
        assertThrows(ForbiddenException.class, () -> hearingNotesService.deleteHearingNote(testHearingId, noteId, invalidCreatedByUuid),
            "User invalid-user-uuid does not have permissions to delete note 1234 on hearing test-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verify(hearingNotesRepository, times(0)).save(any(HearingNoteEntity.class));
    }

    @Test
    void givenNonExistingCommentId_shouldThrowEntityNotFound() {
        var noteId = 1234L;
        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.deleteHearingNote(testHearingId, noteId, createdByUuid),
            "Note 1234 not found for hearing test-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verify(hearingNotesRepository, times(0)).save(any(HearingNoteEntity.class));
    }

    /// ------------

    @Test
    void givenHearingIdAndNoteId_shouldUpdateTheNote() {
        var noteId = 1234L;
        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId("hearingId").id(noteId).createdByUuid("uuid").note("existing note").build();
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().hearingId("hearingId").createdByUuid("uuid").note("existing note updated").build();

        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(existingNote));

        hearingNotesService.updateHearingNote(noteUpdate, noteId);

        verify(hearingNotesRepository).findById(noteId);
        var expected = existingNote.withId(noteId).withNote("existing note updated");
        verify(hearingNotesRepository).save(expected);
        verify(telemetryService).trackUpdateHearingNoteEvent(expected);
    }

    @Test
    void givenHearingIdAndNoteId_andHearingIdDoesNotMatchNoteHearingId_whenUpdateNote_shouldThrowConflictingInput() {
        var noteId = 1234L;
        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId("hearingId").id(noteId).createdByUuid("uuid").note("existing note").build();
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().hearingId("un-matched-hearingId").createdByUuid("uuid").note("existing note updated").build();

        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(existingNote));

        assertThrows(ConflictingInputException.class, () -> hearingNotesService.updateHearingNote(noteUpdate, noteId),
            "Note 1234 not found for hearing invalid-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verifyNoMoreInteractions(hearingNotesRepository);
    }

    @Test
    void givenHearingIdNoteIdAndUserUuid_andUserUuidDoesNotMatchNoteUserUuid_whenUpdateNote_shouldThrowForbiddenException() {
        var noteId = 1234L;
        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId("hearingId").id(noteId).createdByUuid("uuid").note("existing note").build();
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().hearingId("hearingId").createdByUuid("forbidden-uuid").note("existing note updated").build();

        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.of(existingNote));
        assertThrows(ForbiddenException.class, () -> hearingNotesService.updateHearingNote(noteUpdate, noteId),
            "User forbidden-uuid does not have permissions to delete note 1234 on hearing test-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verify(hearingNotesRepository, times(0)).save(any(HearingNoteEntity.class));
    }

    @Test
    void givenNonExistingNoteId_whenUpdateNote_shouldThrowEntityNotFound() {
        var noteId = 1234L;
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().hearingId("hearingId").createdByUuid("forbidden-uuid").note("existing note updated").build();

        given(hearingNotesRepository.findById(noteId)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.updateHearingNote(noteUpdate, noteId),
            "Note 1234 not found for hearing test-hearing-id");
        verify(hearingNotesRepository).findById(noteId);
        verify(hearingNotesRepository, times(0)).save(any(HearingNoteEntity.class));
    }
}