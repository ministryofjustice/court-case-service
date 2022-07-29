package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import java.util.Optional;

@Service
public class DefendantRepositoryFacade {

    private final DefendantRepository defendantRepository;
    private final OffenderRepository offenderRepository;

    public DefendantRepositoryFacade(DefendantRepository defendantRepository, OffenderRepository offenderRepository) {
        this.defendantRepository = defendantRepository;
        this.offenderRepository = offenderRepository;
    }

    public Optional<DefendantEntity> findFirstByDefendantIdOrderByIdDesc(String defendantId) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(defendantId)
            .map(defendantEntity -> {
                Optional.ofNullable(defendantEntity.getCrn())
                    .map(crn -> offenderRepository.findByCrn(crn)
                        .orElseThrow(() -> new RuntimeException(String.format("Unexpected state: Offender with CRN '%s' is specified on defendant '%s' but it does not exist", crn, defendantEntity.getDefendantId())))
                    )
                    .ifPresent(defendantEntity::setOffender);
                return Optional.ofNullable(defendantEntity);
            }).orElse(Optional.empty());
    }
}
