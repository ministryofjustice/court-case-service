package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import java.util.Optional;

@Service
public class DefendantRepositoryFacade {

    private final DefendantRepository defendantRepository;
    public DefendantRepositoryFacade(DefendantRepository defendantRepository) {
        this.defendantRepository = defendantRepository;
    }

    public Optional<DefendantEntity> findFirstByDefendantId(String defendantId) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(defendantId);
    }
}
