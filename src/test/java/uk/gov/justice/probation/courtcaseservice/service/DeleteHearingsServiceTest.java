package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DuplicateHearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteHearingsServiceTest {

    @Mock
    private HearingRepositoryFacade hearingRepositoryFacade;
    @Mock
    private DuplicateHearingRepository duplicateHearingRepository;
    @Mock
    private TelemetryService telemetryService;
    @Mock
    private FeatureFlags featureFlags;

    @InjectMocks
    private DeleteHearingsService deleteHearingsService;

    @Test
    void givenFeatureFlagEnabledAndDuplicateHearings_whenDeleteDuplicateHearing_ThenDuplicateHearingsAreDeleted() {
        when(duplicateHearingRepository.findOldestDuplicateHearings()).thenReturn(duplicateHearings());
        when(featureFlags.deleteHearing()).thenReturn(true);
        deleteHearingsService.deleteDuplicateHearings();
        verify(hearingRepositoryFacade, times(1)).deleteHearing(1L);
        verify(telemetryService, times(1))
                .trackDeleteHearingEvent(eq(TelemetryEventType.PIC_DELETE_HEARING), any(HearingCourtCaseDTO.class), eq(true));
    }

    @Test
    void givenFeatureFlagDisabled_whenDeleteDuplicateHearings_thenNoHearingsDeleted() {
        when(duplicateHearingRepository.findOldestDuplicateHearings()).thenReturn(duplicateHearings());
        when(featureFlags.deleteHearing()).thenReturn(false);
        deleteHearingsService.deleteDuplicateHearings();
        verifyNoInteractions(hearingRepositoryFacade);
        verify(telemetryService, times(1))
                .trackDeleteHearingEvent(eq(TelemetryEventType.PIC_DELETE_HEARING), any(HearingCourtCaseDTO.class), eq(false));
    }

    private List<HearingCourtCaseDTO> duplicateHearings() {
        return List.of(HearingCourtCaseDTO.builder()
                .hearingId("hearing_id")
                .caseId("case_id")
                .id(1L)
                .created(LocalDateTime.of(2024, 11, 23, 12, 0))
                .build());
    }
}