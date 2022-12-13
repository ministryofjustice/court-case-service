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
    void invokeSearchService() {
        String testCrn = "X12345";
        given(caseSearchService.searchByCrn(testCrn)).willReturn(CaseSearchResult.builder().build());
        var actual = searchController.searchByCrn(testCrn);
        verify(caseSearchService).searchByCrn(testCrn);
        assertThat(actual).isNotNull();
    }
}