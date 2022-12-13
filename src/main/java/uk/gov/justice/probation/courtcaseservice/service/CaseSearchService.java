package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.DefendantSearchResultItem;
import uk.gov.justice.probation.courtcaseservice.service.model.SearchResult;

import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchService {

    private final CourtCaseRepository courtCaseRepository;

    @Autowired
    public CaseSearchService(final CourtCaseRepository courtCaseRepository) {
        this.courtCaseRepository = courtCaseRepository;
    }

    @Transactional(readOnly = true)
    public SearchResult searchByCrn(final String crn) {
        return SearchResult.builder().items(courtCaseRepository.findAllCasesByCrn(crn).stream()
            .map(courtCaseEntity -> DefendantSearchResultItem.from(courtCaseEntity, crn))
            .collect(Collectors.toList())).build();
    }
}
