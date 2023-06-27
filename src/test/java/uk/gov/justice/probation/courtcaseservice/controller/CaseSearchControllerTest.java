package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.service.CaseSearchService;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchRequest;
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
    void givenSearchRequest_invokeCaseSearchService() {
        String testCrn = "X12345";
        CaseSearchRequest caseSearchRequest = CaseSearchRequest.builder().term(testCrn).type(CaseSearchType.CRN).build();
        given(caseSearchService.searchCases(caseSearchRequest)).willReturn(CaseSearchResult.builder().build());
        var actual = searchController.searchCases(caseSearchRequest);
        verify(caseSearchService).searchCases(caseSearchRequest);
        assertThat(actual).isNotNull();
    }
}