package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryCustom;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchRequest;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest {
    @Mock
    CaseSearchResultItemMapper caseSearchResultItemMapper;
    @Mock
    private DefendantRepositoryCustom defendantRepositoryCustom;
    @Mock
    private SfoFlagResolver sfoFlagResolver;

    @InjectMocks
    private CaseSearchService caseSearchService;

    @Captor
    private ArgumentCaptor<String> searchTermCaptor;

    final static String TEST_CRN = "X123456";

    @Test
    void shouldFindCasesMatchingTheCrnUsingTheRepo() {
        HearingEntity hearingEntity1 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123456", "case-1", "defendant-id-1");
        HearingEntity hearingEntity2 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123457", "case-2", "defendant-id-2");
        DefendantEntity defendantEntity1 = hearingEntity1.getHearingDefendants().get(0).getDefendant();
        DefendantEntity defendantEntity2 = hearingEntity2.getHearingDefendants().get(0).getDefendant();
        EntityHelper.refreshMappings(hearingEntity1);
        EntityHelper.refreshMappings(hearingEntity2);
        final Pageable pageable = Pageable.ofSize(10).withPage(0);

        given(defendantRepositoryCustom.findDefendantsByCrn(TEST_CRN, pageable))
            .willReturn(new PageImpl<>(List.of(new Pair<>(hearingEntity1.getCourtCase(), defendantEntity1), new Pair<>(hearingEntity2.getCourtCase(), defendantEntity2)), pageable, 2));
        given(sfoFlagResolver.buildSfoFlagsMap(anyList())).willReturn(Map.of());
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), any())).willReturn(null);
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), any())).willReturn(null);
        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").defendantId("defendant-id-1").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").defendantId("defendant-id-2").build();
        given(caseSearchResultItemMapper.from(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), isNull())).willReturn(result1);
        given(caseSearchResultItemMapper.from(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), isNull())).willReturn(result2);

        var actual = caseSearchService.searchCases(CaseSearchRequest.builder().term(TEST_CRN).type(CaseSearchType.CRN).build());

        verify(defendantRepositoryCustom).findDefendantsByCrn(TEST_CRN, pageable);
        verify(caseSearchResultItemMapper).from(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), isNull());
        verify(caseSearchResultItemMapper).from(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), isNull());
        assertThat(actual).isEqualTo(CaseSearchResult.builder().totalElements(2).totalPages(1).items(List.of(result1, result2)).build());
    }

    @Test
    void shouldFindCasesMatchingTheNameUsingTheRepo() {
        HearingEntity hearingEntity1 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123456", "case-1", "defendant-id-1");
        HearingEntity hearingEntity2 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123457", "case-2", "defendant-id-2");
        DefendantEntity defendantEntity1 = hearingEntity1.getHearingDefendants().get(0).getDefendant();
        DefendantEntity defendantEntity2 = hearingEntity2.getHearingDefendants().get(0).getDefendant();
        EntityHelper.refreshMappings(hearingEntity1);
        EntityHelper.refreshMappings(hearingEntity2);
        final Pageable pageable = Pageable.ofSize(10).withPage(0);
        String name = "Jeff";

        given(defendantRepositoryCustom.findDefendantsByName(name, name, pageable))
            .willReturn(new PageImpl<>(List.of(new Pair<>(hearingEntity1.getCourtCase(), defendantEntity1), new Pair<>(hearingEntity2.getCourtCase(), defendantEntity2)), pageable, 2));
        given(sfoFlagResolver.buildSfoFlagsMap(anyList())).willReturn(Map.of());
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), any())).willReturn(null);
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), any())).willReturn(null);
        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").defendantId("defendant-id-1").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").defendantId("defendant-id-2").build();
        given(caseSearchResultItemMapper.from(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), isNull())).willReturn(result1);
        given(caseSearchResultItemMapper.from(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), isNull())).willReturn(result2);

        var actual = caseSearchService.searchCases(CaseSearchRequest.builder().term(name).type(CaseSearchType.NAME).build());

        verify(defendantRepositoryCustom).findDefendantsByName(name, name, pageable);
        verify(caseSearchResultItemMapper).from(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), isNull());
        verify(caseSearchResultItemMapper).from(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), isNull());
        assertThat(actual).isEqualTo(CaseSearchResult.builder().totalElements(2).totalPages(1).items(List.of(result1, result2)).build());
    }

    @Test
    void shouldMapAndReturnTheResultsRetrievedByTheRepository() {
        final Pageable pageable = Pageable.ofSize(10).withPage(0);

        given(defendantRepositoryCustom.findDefendantsByCrn(TEST_CRN, pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        var actual = caseSearchService.searchCases(CaseSearchRequest.builder().term(TEST_CRN).type(CaseSearchType.CRN).build());

        verify(defendantRepositoryCustom).findDefendantsByCrn(TEST_CRN, pageable);
        verifyNoInteractions(caseSearchResultItemMapper);
        assertThat(actual).isEqualTo(CaseSearchResult.builder().totalPages(0).totalElements(0).items(List.of()).build());
    }

    @Test
    void shouldReturnEmptyDataWhenRequestedPageNotAvailable() {
        final Pageable pageable = Pageable.ofSize(10).withPage(2);

        given(defendantRepositoryCustom.findDefendantsByCrn(TEST_CRN, pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        var actual = caseSearchService.searchCases(CaseSearchRequest.builder().page(3).term(TEST_CRN).type(CaseSearchType.CRN).build());

        verify(defendantRepositoryCustom).findDefendantsByCrn(TEST_CRN, pageable);
        verifyNoInteractions(caseSearchResultItemMapper);
        assertThat(actual).isEqualTo(CaseSearchResult.builder().totalPages(0).totalElements(0).items(List.of()).build());
    }

    @Test
    void shouldCaptureLegalSearchTermEvenWithWhiteSpaces() {
        String expectedSearchTerm = " TEST TEST ";

        HearingEntity hearingEntity1 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123456", "case-1", "defendant-id-1");
        HearingEntity hearingEntity2 = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("C123457", "case-2", "defendant-id-2");
        DefendantEntity defendantEntity1 = hearingEntity1.getHearingDefendants().get(0).getDefendant();
        DefendantEntity defendantEntity2 = hearingEntity2.getHearingDefendants().get(0).getDefendant();
        EntityHelper.refreshMappings(hearingEntity1);
        EntityHelper.refreshMappings(hearingEntity2);
        final Pageable pageable = Pageable.ofSize(10).withPage(0);

        given(defendantRepositoryCustom.findDefendantsByName(searchTermCaptor.capture(), any(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(new Pair<>(hearingEntity1.getCourtCase(), defendantEntity1), new Pair<>(hearingEntity2.getCourtCase(), defendantEntity2)), pageable, 2));
        given(sfoFlagResolver.buildSfoFlagsMap(anyList())).willReturn(Map.of());
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), any())).willReturn(null);
        given(sfoFlagResolver.resolveSfoFlag(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), any())).willReturn(null);
        CaseSearchResultItem result1 = CaseSearchResultItem.builder().defendantName("X").defendantId("defendant-id-1").build();
        CaseSearchResultItem result2 = CaseSearchResultItem.builder().defendantName("Y").defendantId("defendant-id-2").build();
        given(caseSearchResultItemMapper.from(eq(hearingEntity1.getCourtCase()), eq(defendantEntity1), isNull())).willReturn(result1);
        given(caseSearchResultItemMapper.from(eq(hearingEntity2.getCourtCase()), eq(defendantEntity2), isNull())).willReturn(result2);

        caseSearchService.searchCases(CaseSearchRequest.builder().term(expectedSearchTerm).type(CaseSearchType.NAME).build());

        verify(defendantRepositoryCustom).findDefendantsByName(searchTermCaptor.capture(), any(), any(Pageable.class));
        assertEquals("TEST & TEST", searchTermCaptor.getValue());
    }
}
