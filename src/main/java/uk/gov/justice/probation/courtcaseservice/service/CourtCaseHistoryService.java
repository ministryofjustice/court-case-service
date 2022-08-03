package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseHistory;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourtCaseHistoryService {

    private final CourtCaseRepository courtCaseRepository;
    private final DefendantRepositoryFacade defendantRepositoryFacade;

    public CourtCaseHistoryService(CourtCaseRepository courtCaseRepository,
                                   DefendantRepositoryFacade defendantRepositoryFacade) {
        this.courtCaseRepository = courtCaseRepository;
        this.defendantRepositoryFacade = defendantRepositoryFacade;
    }

    public CourtCaseHistory getCourtCaseHistory(String caseId) {
        return courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
            .map(courtCaseEntity -> getCourtCaseHistory(courtCaseEntity))
            .orElseThrow(() -> new EntityNotFoundException("Court case with id {} does not exist", caseId));
    }

    private CourtCaseHistory getCourtCaseHistory(CourtCaseEntity courtCaseEntity) {
        var uniqueDefendantIds = courtCaseEntity.getHearings().stream().map(HearingEntity::getHearingDefendants)
            .flatMap(List::stream).map(HearingDefendantEntity::getDefendantId).collect(Collectors.toSet());

        var defendantEntities = uniqueDefendantIds.stream().map(s -> defendantRepositoryFacade.findFirstByDefendantIdOrderByIdDesc(s))
            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        return CourtCaseHistory.of(courtCaseEntity, defendantEntities);
    }
}
