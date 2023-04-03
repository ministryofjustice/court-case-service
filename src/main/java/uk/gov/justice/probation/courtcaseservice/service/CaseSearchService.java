package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;

import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchService {

    private final CourtCaseRepository courtCaseRepository;
    private final CaseSearchResultItemMapper caseSearchResultItemMapper;

    @Autowired
    public CaseSearchService(final CourtCaseRepository courtCaseRepository,
                             final CaseSearchResultItemMapper caseSearchResultItemMapper) {
        this.courtCaseRepository = courtCaseRepository;
        this.caseSearchResultItemMapper = caseSearchResultItemMapper;
    }

    @Transactional(readOnly = true)
    public CaseSearchResult searchCases(final String searchTerm) {

        var searchTypeResolver = CaseSearchTypeResolver.get(searchTerm);

        var result = switch (searchTypeResolver.getType()) {
            case CRN -> courtCaseRepository.findAllCasesByCrn(searchTypeResolver.getSearchTerm());
            case NAME -> courtCaseRepository.findAllCasesByFirstNameAndLastName(searchTypeResolver.getSearchTerm(), searchTerm.trim());
        };

        return CaseSearchResult.builder().items(result.stream()
            .map(courtCaseEntity -> caseSearchResultItemMapper.from(courtCaseEntity, searchTerm))
            .collect(Collectors.toList())).build();
    }
}
