package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.SearchResult;
import uk.gov.justice.probation.courtcaseservice.service.model.DefendantSearchResultItem;

import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class DefendantSearchService {

    private final CourtCaseRepository courtCaseRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public DefendantSearchService(final CourtCaseRepository courtCaseRepository,
                                  final OffenderRepository offenderRepository) {
        this.courtCaseRepository = courtCaseRepository;
        this.offenderRepository = offenderRepository;
    }
    @Transactional(readOnly = true)
    public SearchResult searchByCrn(final String cnr) {
        return offenderRepository.findByCrn(cnr)
            .map(DefendantSearchService::buildSearchResponse)
            .orElseGet(() -> {
                throw new EntityNotFoundException("CRN %s not found");
            });
    }

    private static SearchResult buildSearchResponse(OffenderEntity offenderEntity) {

        offenderEntity.getDefendants().stream().map(defendantEntity -> defendantEntity.getHearingDefendants())
            .flatMap(hearingDefendantEntities -> hearingDefendantEntities.stream());

        throw new UnsupportedOperationException();
    }

    @Transactional(readOnly = true)
    public SearchResult searchByCrnUsingCourtCase(final String crn) {
        return SearchResult.builder().defendants(courtCaseRepository.findAllCasesByCrn(crn).stream()
            .map(courtCaseEntity -> DefendantSearchResultItem.from(courtCaseEntity, crn))
            .collect(Collectors.toList())).build();
    }
}
