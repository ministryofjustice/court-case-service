package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourtCaseHistoryServiceTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private DefendantRepositoryFacade defendantRepositoryFacade;
    @Mock
    private OffenderRepository offenderRepository;

    @InjectMocks
    private CourtCaseHistoryService courtCaseHistoryService;

    @Test
    void shouldReturnCourtCaseHistory() {
        String caseId = "case-id-one";
        given(defendantRepositoryFacade.findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID))
            .willReturn(Optional.of(EntityHelper.aDefendantEntity()));
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId))
            .willReturn(Optional.of(CourtCaseEntity.builder().sourceType(SourceType.LIBRA)
                .hearings(List.of(EntityHelper.aHearingEntity(caseId))).build()));
        courtCaseHistoryService.getCourtCaseHistory(caseId);
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(caseId);
        verify(defendantRepositoryFacade).findFirstByDefendantIdOrderByIdDesc(EntityHelper.DEFENDANT_ID);
    }

    @Test
    void shouldThroughEntityNotFoundExceptionWhenCourtCaseNotFound() {
        String caseId = "case-id-one";
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId))
            .willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> courtCaseHistoryService.getCourtCaseHistory(caseId));
    }
}