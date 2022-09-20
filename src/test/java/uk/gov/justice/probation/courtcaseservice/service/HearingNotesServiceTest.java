package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Test
    void givenHearingIdExistsInDatabase_shouldCreateAHearingNote() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().hearingId("hearingId").build();

        given(hearingRepository.findFirstByHearingIdOrderByIdDesc("hearingId")).willReturn(Optional.of(HearingEntity.builder().build()));
        given(hearingNotesRepository.save(hearingNoteEntity)).willReturn(hearingNoteEntity);


        hearingNotesService.createHearingNote(hearingNoteEntity);

        verify(hearingRepository).findFirstByHearingIdOrderByIdDesc("hearingId");
        verify(hearingNotesRepository).save(hearingNoteEntity);
    }

    @Test
    void givenHearingIdDoesNotExistsInDatabase_shouldThrowEntityNotFound() {
        HearingNoteEntity hearingNoteEntity = HearingNoteEntity.builder().hearingId("hearingId").build();

        given(hearingRepository.findFirstByHearingIdOrderByIdDesc("hearingId")).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            hearingNotesService.createHearingNote(hearingNoteEntity);
        }, "Hearing hearingId not found");

        verify(hearingRepository).findFirstByHearingIdOrderByIdDesc("hearingId");
        verifyNoInteractions(hearingNotesRepository);
    }

}