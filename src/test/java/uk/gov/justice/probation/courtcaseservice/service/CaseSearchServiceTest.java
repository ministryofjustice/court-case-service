package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest {
    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    CaseSearchResultItemMapper caseSearchResultItemMapper;

    @InjectMocks
    private CaseSearchService caseSearchService;

    final static String TEST_CRN = "X123456";

    @Test
    void shouldFindCasesMatchingTheCrnUsingTheRepo() {

        HearingEntity hearingEntity1 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123456", "case-1", "defendant-id-1");
        HearingEntity hearingEntity2 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123457", "case-2", "defendant-id-2");
        DefendantEntity defendantEntity1 = hearingEntity1.getHearingDefendants().get(0).getDefendant();
        DefendantEntity defendantEntity2 = hearingEntity2.getHearingDefendants().get(0).getDefendant();
        EntityHelper.refreshMappings(hearingEntity1);
        EntityHelper.refreshMappings(hearingEntity2);

        given(defendantRepository.findDefendantsByCrn(TEST_CRN)).willReturn(List.of(defendantEntity1, defendantEntity2));

        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").defendantId("defendant-id-1").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").defendantId("defendant-id-2").build();

        given(caseSearchResultItemMapper.from(hearingEntity1.getCourtCase(), "defendant-id-1")).willReturn(result1);
        given(caseSearchResultItemMapper.from(hearingEntity2.getCourtCase(), "defendant-id-2")).willReturn(result2);

        var actual = caseSearchService.searchCases(TEST_CRN, CaseSearchType.CRN);

        verify(defendantRepository).findDefendantsByCrn(TEST_CRN);
        verify(caseSearchResultItemMapper).from(hearingEntity1.getCourtCase(), defendantEntity1.getDefendantId());
        verify(caseSearchResultItemMapper).from(hearingEntity2.getCourtCase(), defendantEntity2.getDefendantId());

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of(result1, result2)).build());
    }

    @Test
    void shouldFindCasesMatchingTheNameUsingTheRepo() {

        HearingEntity hearingEntity1 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123456", "case-1", "defendant-id-1");
        HearingEntity hearingEntity2 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123457", "case-2", "defendant-id-2");
        DefendantEntity defendantEntity1 = hearingEntity1.getHearingDefendants().get(0).getDefendant();
        DefendantEntity defendantEntity2 = hearingEntity2.getHearingDefendants().get(0).getDefendant();
        EntityHelper.refreshMappings(hearingEntity1);
        EntityHelper.refreshMappings(hearingEntity2);

        String tsQueryString = "Jeff & Bloggs";
        String name = "Jeff Bloggs";
        given(defendantRepository.findDefendantsByName(tsQueryString, name)).willReturn(List.of(defendantEntity1, defendantEntity2));

        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").defendantId("defendant-id-1").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").defendantId("defendant-id-2").build();

        given(caseSearchResultItemMapper.from(hearingEntity1.getCourtCase(), "defendant-id-1")).willReturn(result1);
        given(caseSearchResultItemMapper.from(hearingEntity2.getCourtCase(), "defendant-id-2")).willReturn(result2);

        var actual = caseSearchService.searchCases(name, CaseSearchType.NAME);

        verify(defendantRepository).findDefendantsByName(tsQueryString, name);
        verify(caseSearchResultItemMapper).from(hearingEntity1.getCourtCase(), "defendant-id-1");
        verify(caseSearchResultItemMapper).from(hearingEntity2.getCourtCase(), "defendant-id-2");

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of(result1, result2)).build());
    }

    @Test
    void shouldMapAndReturnTheResultsRetrievedByTheRepository() {
        given(defendantRepository.findDefendantsByCrn(TEST_CRN)).willReturn(List.of());

        var actual = caseSearchService.searchCases(TEST_CRN, CaseSearchType.CRN);

        verify(defendantRepository).findDefendantsByCrn(TEST_CRN);
        verifyNoInteractions(caseSearchResultItemMapper);

        assertThat(actual).isEqualTo(CaseSearchResult.builder().items(List.of()).build());
    }
}
