package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderUpdateService {

    private final DefendantRepository defendantRepository;
    private final OffenderRepository offenderRepository;
    private final OffenderRepositoryFacade offenderRepositoryFacade;

    @Autowired
    public OffenderUpdateService(DefendantRepository defendantRepository, OffenderRepository offenderRepository, OffenderRepositoryFacade offenderRepositoryFacade) {
        this.defendantRepository = defendantRepository;
        this.offenderRepository = offenderRepository;
        this.offenderRepositoryFacade = offenderRepositoryFacade;
    }

    @Transactional
    public void removeDefendantOffenderAssociation(final String defendantId) {
        final var defendant = findDefendantOrElseThrow(defendantId);

        if (!StringUtils.isEmpty(defendant.getCrn())) {
            defendantRepository.save(defendant.withCrn(null).withId(null));
        }
    }

    public Mono<OffenderEntity> getDefendantOffenderByDefendantId(final String defendantId) {

        final var defendant = findDefendantOrElseThrow(defendantId);
        Optional<OffenderEntity> offenderEntity = StringUtils.isEmpty(defendant.getCrn()) ? Optional.empty() :
            offenderRepository.findByCrn(defendant.getCrn());

        if(offenderEntity.isEmpty()) {
            throw new EntityNotFoundException("Offender details not found for defendant %s", defendantId);
        }
        return Mono.just(offenderEntity.get());
    }

    @Transactional
    public Mono<OffenderEntity> updateDefendantOffender(final String defendantId, OffenderEntity offenderUpdate) {

        final var defendant = findDefendantOrElseThrow(defendantId);

        final var offenderToUpdate = offenderRepositoryFacade.updateOffenderIfItExists(offenderUpdate);

        final var updatedOffender = offenderRepository.save(offenderToUpdate);

        if (!StringUtils.equals(defendant.getCrn(), offenderToUpdate.getCrn())) {
            final var updatedDefendant = defendant.withCrn(offenderUpdate.getCrn()).withId(null);
            defendantRepository.save(updatedDefendant);
        }

        return Mono.just(updatedOffender);
    }

    private DefendantEntity findDefendantOrElseThrow(String defendantId) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(defendantId)
                .orElseThrow(() -> new EntityNotFoundException("Defendant with id %s does not exist", defendantId));
    }
}
