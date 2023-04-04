package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.service.CaseSearchService;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;

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
    void invokeCommonSearchService() {
        String testCrn = "X12345";
        given(caseSearchService.searchCases(testCrn)).willReturn(CaseSearchResult.builder().build());
        var actual = searchController.searchCases(testCrn);
        verify(caseSearchService).searchCases(testCrn);
        assertThat(actual).isNotNull();
    }
}