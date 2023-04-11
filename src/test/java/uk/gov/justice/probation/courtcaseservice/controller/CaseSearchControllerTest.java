package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.service.CaseSearchService;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseSearchControllerTest {

    @Mock
    CaseSearchService caseSearchService;
    @InjectMocks
    CaseSearchController searchController;

    @Test
    void givenSearchTypeAndTerm_invokeCaseSearchService() {
        String testCrn = "X12345";
        given(caseSearchService.searchCases(testCrn, CaseSearchType.CRN)).willReturn(CaseSearchResult.builder().build());
        var actual = searchController.searchCases(testCrn, CaseSearchType.CRN);
        verify(caseSearchService).searchCases(testCrn, CaseSearchType.CRN);
        assertThat(actual).isNotNull();
    }
}