package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryCustom;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchRequest;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchService {

    private final CaseSearchResultItemMapper caseSearchResultItemMapper;
    private final DefendantRepositoryCustom defendantRepositoryCustom;
    private final SeriousFurtherOffenceFlagResolver seriousFurtherOffenceFlagResolver;

    @Autowired
    public CaseSearchService(final DefendantRepositoryCustom defendantRepositoryCustom,
                             final CaseSearchResultItemMapper caseSearchResultItemMapper,
                             final SeriousFurtherOffenceFlagResolver seriousFurtherOffenceFlagResolver) {
        this.caseSearchResultItemMapper = caseSearchResultItemMapper;
        this.defendantRepositoryCustom = defendantRepositoryCustom;
        this.seriousFurtherOffenceFlagResolver = seriousFurtherOffenceFlagResolver;
    }

    @Transactional(readOnly = true)
    public CaseSearchResult searchCases(CaseSearchRequest caseSearchRequest) {
        Pageable pageable = Pageable.ofSize(caseSearchRequest.getSize()).withPage(caseSearchRequest.getPage() - 1);

        final String searchTerm = caseSearchRequest.getTerm();
        var resultsPage = switch (caseSearchRequest.getType()) {
            case CRN -> defendantRepositoryCustom.findDefendantsByCrn(searchTerm, pageable);
            case NAME ->
                defendantRepositoryCustom.findDefendantsByName(Arrays.stream(searchTerm.trim().replaceAll("\\s+", " ").split(" "))
                    // Remove leading and trailing whitespaces
                    .map(String::trim).collect(Collectors.joining(" & ")), searchTerm.strip(), pageable);
        };

        if(caseSearchRequest.getPage() > resultsPage.getTotalPages()) {
            return CaseSearchResult.builder()
                .totalElements(resultsPage.getTotalElements())
                .totalPages(resultsPage.getTotalPages())
                .items(List.of())
                .build();
        }

        var seriousFurtherOffenceFlagsByCode = seriousFurtherOffenceFlagResolver.buildSeriousFurtherOffenceFlagsMap(resultsPage.getContent());

        return CaseSearchResult.builder()
            .totalElements(resultsPage.getTotalElements())
            .totalPages(resultsPage.getTotalPages())
            .items(resultsPage.getContent().stream()
                .map(pair -> caseSearchResultItemMapper.from(pair.getFirst(), pair.getSecond(), seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(pair.getFirst(), pair.getSecond(), seriousFurtherOffenceFlagsByCode)))
                .collect(Collectors.toList())).build();
    }
}
