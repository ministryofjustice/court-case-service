package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest {
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    CaseSearchResultItemMapper caseSearchResultItemMapper;

    @InjectMocks
    private CaseSearchService caseSearchService;

    final static String TEST_CRN = "X12345";

    @Test
    void shouldFindCasesMatchingTheCrnUsingTheRepo() {

        var crnArgCapture = ArgumentCaptor.forClass(String.class);
        var caseArgCapture = ArgumentCaptor.forClass(CourtCaseEntity.class);

        CourtCaseEntity case1 = CourtCaseEntity.builder().caseId("case-id-1").build();
        CourtCaseEntity case2 = CourtCaseEntity.builder().caseId("case-id-2").build();
        given(courtCaseRepository.findAllCasesByCrn(TEST_CRN)).willReturn(List.of(case1, case2));

        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").build();

        given(caseSearchResultItemMapper.from(case1, TEST_CRN)).willReturn(result1).willReturn(result2);

        var actual = caseSearchService.searchByCrn(TEST_CRN);

        verify(courtCaseRepository).findAllCasesByCrn(TEST_CRN);
        verify(caseSearchResultItemMapper, times(2)).from(caseArgCapture.capture(), crnArgCapture.capture());
        assertThat(caseArgCapture.getAllValues()).isEqualTo(List.of(case1, case2));
        assertThat(crnArgCapture.getAllValues()).isEqualTo(List.of(TEST_CRN, TEST_CRN));

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of(result1, result2)).build());
    }

    @Test
    void shouldMapAndReturnTheResultsRetrievedByTheRepository() {
        given(courtCaseRepository.findAllCasesByCrn(TEST_CRN)).willReturn(List.of());

        var actual = caseSearchService.searchByCrn(TEST_CRN);

        verify(courtCaseRepository).findAllCasesByCrn(TEST_CRN);
        verifyNoInteractions(caseSearchResultItemMapper);

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of()).build());
    }
}