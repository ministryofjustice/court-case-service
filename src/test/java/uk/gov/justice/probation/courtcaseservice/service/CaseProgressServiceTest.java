package uk.gov.justice.probation.courtcaseservice.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseProgressMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CaseProgressServiceTest {

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private CaseProgressMapper caseProgressMapper;

    @InjectMocks
    private CaseProgressService caseProgressService;

    private static final String CASE_ID = "test-case-id";

    @Test
    void givenCaseId_shouldFetchAllHearingsUsingHearingRepo_thenMapToCaseProgressHearing() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).willReturn(Optional.of(CourtCaseEntity.builder().build()));
        List<HearingEntity> hearingEntities = List.of(HearingEntity.builder().hearingId("hearing-id-one").build(),
            HearingEntity.builder().hearingId("hearing-id-one").build());
        given(hearingRepository.findHearingsByCaseId(CASE_ID)).willReturn(
            hearingEntities);
        List<CaseProgressHearing> caseProgressHearings = List.of(CaseProgressHearing.builder().hearingId("test-hearing-one").build(),
            CaseProgressHearing.builder().hearingId("test-hearing-two").build());
        given(caseProgressMapper.mapFrom(any())).willReturn(
            caseProgressHearings);

        var progress = caseProgressService.getCaseHearingProgress(CASE_ID);

        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(CASE_ID);
        verify(hearingRepository).findHearingsByCaseId(CASE_ID);
        verify(caseProgressMapper).mapFrom(hearingEntities);

        Assertions.assertThat(progress).isEqualTo(caseProgressHearings);
    }

    @Test
    void givenCaseId_caseDoesNotExist_thenThrowEntityNotFoundException() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).willReturn(Optional.empty());

        assertThrowsExactly(EntityNotFoundException.class, () -> caseProgressService.getCaseHearingProgress(CASE_ID),
            "Court case with id test-case-id does not exist"
        );
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(CASE_ID);
        verifyNoInteractions(hearingRepository);
        verifyNoInteractions(caseProgressMapper);
    }
}