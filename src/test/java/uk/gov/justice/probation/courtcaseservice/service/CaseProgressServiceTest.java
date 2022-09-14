package uk.gov.justice.probation.courtcaseservice.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseProgressMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseProgressServiceTest {

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private CaseProgressMapper caseProgressMapper;

    @InjectMocks
    private CaseProgressService caseProgressService;

    private static final String CASE_ID = "test-case-id";

    @Test
    void givenCaseId_shouldFetchAllHearingsUsingHearingRepo_thenMapToCaseProgressHearing() {
        List<HearingEntity> hearingEntities = List.of(HearingEntity.builder().hearingId("hearing-id-one").build(),
            HearingEntity.builder().hearingId("hearing-id-one").build());
        given(hearingRepository.findHearingsByCaseId(CASE_ID)).willReturn(
            Optional.of(hearingEntities));
        List<CaseProgressHearing> caseProgressHearings = List.of(CaseProgressHearing.builder().hearingId("test-hearing-one").build(),
            CaseProgressHearing.builder().hearingId("test-hearing-two").build());
        given(caseProgressMapper.mapFrom(any())).willReturn(
            caseProgressHearings);

        var progress = caseProgressService.getCaseHearingProgress(CASE_ID);

        verify(hearingRepository).findHearingsByCaseId(CASE_ID);
        verify(caseProgressMapper).mapFrom(hearingEntities);

        Assertions.assertThat(progress).isEqualTo(caseProgressHearings);
    }
}