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

import jakarta.transaction.Transactional;
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
        defendant.confirmNoMatch();
        defendantRepository.save(defendant);
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

        var defendant = findDefendantOrElseThrow(defendantId);

        final var updatedOffender = offenderRepositoryFacade.save(offenderUpdate);

        if (!StringUtils.equals(defendant.getCrn(), updatedOffender.getCrn())) {
            defendant.confirmMatch(updatedOffender);
            defendantRepository.save(defendant);
        }

        return Mono.just(updatedOffender);
    }

    private DefendantEntity findDefendantOrElseThrow(String defendantId) {
        return defendantRepository.findFirstByDefendantId(defendantId)
                .orElseThrow(() -> new EntityNotFoundException("Defendant with id %s does not exist", defendantId));
    }
}
