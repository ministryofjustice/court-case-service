package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourtCaseHistoryServiceTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private DefendantRepository defendantRepository;

    @InjectMocks
    private CourtCaseHistoryService courtCaseHistoryService;

    @Test
    void shouldReturnCourtCaseHistory() {
        String caseId = "case-id-one";
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId))
            .willReturn(Optional.of(CourtCaseEntity.builder().sourceType(SourceType.LIBRA)
                .hearings(List.of(EntityHelper.aHearingEntity(caseId))).build()));
        courtCaseHistoryService.getCourtCaseHistory(caseId);
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(caseId);
        verify(defendantRepository).findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID);
    }

    @Test
    void shouldThroughEntityNotFoundExceptionWhenCourtCaseNotFound() {
        String caseId = "case-id-one";
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId))
            .willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> courtCaseHistoryService.getCourtCaseHistory(caseId));
    }
}