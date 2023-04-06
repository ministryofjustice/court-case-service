package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchType;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchService {

    private final DefendantRepository defendantRepository;
    private final CaseSearchResultItemMapper caseSearchResultItemMapper;

    @Autowired
    public CaseSearchService(final DefendantRepository defendantRepository,
                             final CaseSearchResultItemMapper caseSearchResultItemMapper) {
        this.defendantRepository = defendantRepository;
        this.caseSearchResultItemMapper = caseSearchResultItemMapper;
    }

    @Transactional(readOnly = true)
    public CaseSearchResult searchCases(final String searchTerm, final CaseSearchType searchType) {

        var defendants = switch (searchType) {
            case CRN -> defendantRepository.findDefendantsByCrn(searchTerm);
            case NAME -> defendantRepository.findDefendantsByName(Arrays.stream(searchTerm.split(" ")).map(s -> s.trim()).collect(Collectors.joining(" & ")), searchTerm.trim());
        };

        // extract cases that defendants are involved in
        var courtCases = defendants.stream().map(DefendantEntity::getHearingDefendants)
            .flatMap(Collection::stream).map(hearingDefendantEntity -> new Pair<>(hearingDefendantEntity.getDefendant().getDefendantId(), hearingDefendantEntity.getHearing().getCourtCase()))
            .collect(Collectors.toList());

        return CaseSearchResult.builder().items(courtCases.stream()
            .map(courtCaseEntity -> caseSearchResultItemMapper.from(courtCaseEntity.getSecond(), courtCaseEntity.getFirst()))
            .collect(Collectors.toList())).build();
    }
}
