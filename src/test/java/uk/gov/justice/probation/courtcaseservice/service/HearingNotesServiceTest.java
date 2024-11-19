package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID;

@ExtendWith(MockitoExtension.class)
class HearingNotesServiceTest {

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private HearingNotesRepository hearingNotesRepository;
    @Mock
    private TelemetryService telemetryService;

    @Mock
    private HearingEntityInitService hearingNotesServiceInitService;

    @InjectMocks
    private HearingNotesService hearingNotesService;

    private static final String testHearingId = "test-hearing-id";
    private static final String createdByUuid = "test-user-uuid";
    private static final HearingNoteEntity hearingNote = HearingNoteEntity.builder().hearingId(testHearingId).author("test author").createdByUuid(createdByUuid).build();
    @Captor
    private ArgumentCaptor<HearingNoteEntity> noteCaptor;

    private static final String CREATED_BY_UUID = "uuid";

    @Test
    void givenHearingIdExistsInDatabase_andADraftNoteDoNotExists_shouldCreateNote() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().hearingId("hearingId").createdByUuid(createdByUuid).build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        given(hearingNotesRepository.save(hearingNoteEntity)).willReturn(hearingNoteEntity);


        hearingNotesService.createHearingNote(HEARING_ID, EntityHelper.DEFENDANT_ID, hearingNoteEntity);

        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verify(hearingNotesRepository).save(hearingNoteEntity);
        verify(telemetryService).trackCreateHearingNoteEvent(hearingNoteEntity);
    }

    @Test
    void givenHearingIdExistsInDatabase_andADraftNoteExists_shouldUpdateNoteAndRemoveDraftFlag() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().createdByUuid(createdByUuid).note("word1 word2 word3").build();
        HearingNoteEntity dbHearingNoteEntity = HearingNoteEntity.builder().hearingId(testHearingId).createdByUuid(createdByUuid).note("word1 word2").draft(true).build();

        HearingEntity hearing = EntityHelper.aHearingEntity()
            .withHearingDefendants(List.of(EntityHelper.aHearingDefendantEntity().withNotes(EntityHelper.getMutableList(List.of(dbHearingNoteEntity)))));

        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        given(hearingNotesRepository.save(noteCaptor.capture())).willReturn(dbHearingNoteEntity);

        hearingNotesService.createHearingNote(HEARING_ID, EntityHelper.DEFENDANT_ID, hearingNoteEntity);
        assertThat(noteCaptor.getValue()).isEqualTo(dbHearingNoteEntity.withNote("word1 word2 word3").withDraft(false));
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verify(hearingNotesRepository).save(dbHearingNoteEntity);
    }

    @Test
    void givenHearingIdDoesNotExistsInDatabase_shouldThrowEntityNotFound() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().build();

        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> hearingNotesService.createHearingNote(HEARING_ID, EntityHelper.DEFENDANT_ID, hearingNoteEntity), "Hearing hearingId not found");

        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verifyNoInteractions(hearingNotesRepository);
    }

    // --- START createOrUpdateHearingNoteDraft

    @Test
    void givenHearingNoteDoNotExist_whenCreateOrUpdateHearingNoteDraft_createANewHearingNoteDraft() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().createdByUuid(createdByUuid).build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        given(hearingNotesRepository.save(noteCaptor.capture())).willReturn(hearingNoteEntity);

        hearingNotesService.createOrUpdateHearingNoteDraft(HEARING_ID, EntityHelper.DEFENDANT_ID, hearingNoteEntity);
        HearingNoteEntity dbNote = noteCaptor.getValue();
        assertThat(dbNote.isDraft()).isTrue();
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verify(hearingNotesRepository).save(dbNote);
    }

    @Test
    void givenHearingIdDoesNotExistsInDatabase__whenCreateOrUpdateHearingNoteDraft_shouldThrowEntityNotFound() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().build();

        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> hearingNotesService.createOrUpdateHearingNoteDraft(HEARING_ID, EntityHelper.DEFENDANT_ID, hearingNoteEntity), "Hearing hearingId not found");

        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verifyNoInteractions(hearingNotesRepository);
    }

    @Test
    void givenHearingIdAndNoteId_shouldMarkNoteAsDeleted() {
        var noteId = 1234L;
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().id(noteId).createdByUuid(createdByUuid).build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(hearingNoteEntity);
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        given(hearingNotesRepository.save(noteCaptor.capture())).willReturn(hearingNoteEntity);

        hearingNotesService.deleteHearingNote(HEARING_ID, DEFENDANT_ID, noteId, createdByUuid);
        assertTrue(hearingNoteEntity.isDeleted());
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verify(hearingNotesRepository).save(hearingNoteEntity);
        verify(telemetryService).trackDeleteHearingNoteEvent(hearingNoteEntity);
    }

    @Test
    void givenHearingIdNoteIdAndUserUuid_andUserUuidDoesNotMatchNoteUserUuid_shouldThrowNotFound() {
        var noteId = 1234L;
        String invalidCreatedByUuid = "invalid-user-uuid";

        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().id(noteId).createdByUuid(createdByUuid).build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(hearingNoteEntity);
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));

        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.deleteHearingNote(HEARING_ID, DEFENDANT_ID, noteId, invalidCreatedByUuid),
            "Note 1234 not found for hearing 75e63d6c-5487-4244-a5bc-7cf8a38992db, defendant d1eefed2-04df-11ec-b2d8-0242ac130002 and user uuid invalid-user-uuid or the user does not have permissions to modify");
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verifyNoInteractions(hearingNotesRepository);
    }

    @Test
    void givenNonExistingNoteId_shouldThrowEntityNotFound() {
        var noteId = 1234L;

        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().id(noteId).createdByUuid(createdByUuid).build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        hearing.getHearingDefendants().get(0).getNotes().add(hearingNoteEntity);

        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.deleteHearingNote(HEARING_ID, DEFENDANT_ID, 567L, createdByUuid),
            "Note 567 not found for hearing 75e63d6c-5487-4244-a5bc-7cf8a38992db, defendant d1eefed2-04df-11ec-b2d8-0242ac130002 and user uuid test-user-uuid or the user does not have permissions to modify\n");
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verifyNoInteractions(hearingNotesRepository);
    }

    /// ------------

    @Test
    void givenHearingIdAndNoteId_shouldUpdateTheNote() {
        var noteId = 1234L;

        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId(HEARING_ID).id(noteId).createdByUuid(CREATED_BY_UUID).note("existing note").build();
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().hearingId("hearingId").createdByUuid(CREATED_BY_UUID).note("existing note updated").build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(existingNote);


        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));
        given(hearingNotesRepository.save(noteCaptor.capture())).willReturn(existingNote);

        hearingNotesService.updateHearingNote(HEARING_ID, DEFENDANT_ID, noteUpdate, noteId);

        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        var expected = existingNote.withId(noteId).withNote("existing note updated");
        verify(hearingNotesRepository).save(expected);
        verify(telemetryService).trackUpdateHearingNoteEvent(expected);
    }

    @Test
    void givenHearingIdNoteIdAndUserUuid_andUserUuidDoesNotMatchNoteUserUuid_whenUpdateNote_shouldThrowForbiddenException() {
        var noteId = 1234L;
        HearingNoteEntity existingNote = HearingNoteEntity.builder().id(noteId).createdByUuid("uuid").note("existing note").build();
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().createdByUuid("forbidden-uuid").note("existing note updated").build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(existingNote);

        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));

        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.updateHearingNote(HEARING_ID, DEFENDANT_ID, noteUpdate, noteId),
            "User forbidden-uuid does not have permissions to delete note 1234 on hearing test-hearing-id");
        verifyNoMoreInteractions(hearingNotesRepository);
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
    }

    @Test
    void givenNonExistingNoteId_whenUpdateNote_shouldThrowEntityNotFound() {
        var noteId = 1234L;
        HearingNoteEntity noteUpdate = HearingNoteEntity.builder().createdByUuid("forbidden-uuid").note("existing note updated").build();

        HearingEntity hearing = EntityHelper.aHearingEntity();

        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));

        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.updateHearingNote(HEARING_ID, DEFENDANT_ID, noteUpdate, noteId),
            "Note 1234 not found for hearing test-hearing-id");
        verify(hearingNotesRepository, times(0)).save(any(HearingNoteEntity.class));
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
    }

    @Test
    void givenDraftNoteExistsForAUser_when_deleteDraft_shouldDeleteDraftNote() {

        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId(HEARING_ID).draft(true).createdByUuid(CREATED_BY_UUID).note("existing note").build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(existingNote);
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));

        hearingNotesService.deleteHearingNoteDraft(HEARING_ID, DEFENDANT_ID, CREATED_BY_UUID);

        assertTrue(existingNote.isDeleted());
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verify(hearingNotesRepository).save(existingNote);
    }

    @Test
    void givenDraftDoesNotExistsForAUser_when_deleteDraft_shouldThrowNotFoundError() {
        HearingNoteEntity existingNote = HearingNoteEntity.builder().hearingId(HEARING_ID).draft(true).createdByUuid(CREATED_BY_UUID).note("existing note").build();

        HearingEntity hearing = EntityHelper.aHearingEntity();
        hearing.getHearingDefendants().get(0).getNotes().add(existingNote);
        given(hearingNotesServiceInitService.findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID)).willReturn(Optional.of(hearing));

        String differentUser = "different_user";
        assertThrows(EntityNotFoundException.class, () -> hearingNotesService.deleteHearingNoteDraft(HEARING_ID, DEFENDANT_ID, differentUser),
            String.format("Draft note not found for user %s on hearing %s / defendant %s", differentUser, HEARING_ID, DEFENDANT_ID));
        verify(hearingNotesServiceInitService).findFirstByHearingIdAndInitHearingNotes(HEARING_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(hearingNotesRepository);
    }
}