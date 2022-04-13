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
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import javax.transaction.Transactional;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderUpdateService {

    private final DefendantRepository defendantRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public OffenderUpdateService(DefendantRepository defendantRepository, OffenderRepository offenderRepository) {
        this.defendantRepository = defendantRepository;
        this.offenderRepository = offenderRepository;
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

        final var offenderEntity =  StringUtils.isEmpty(defendant.getCrn()) ? OffenderEntity.builder().build() :
                offenderRepository.findByCrn(defendant.getCrn()).orElse(OffenderEntity.builder().build());

        return Mono.just(offenderEntity);
    }

    @Transactional
    public Mono<OffenderEntity> updateDefendantOffender(final String defendantId, OffenderEntity offenderUpdate) {

        final var defendant = findDefendantOrElseThrow(defendantId);

        final var dbOffender = offenderRepository.findByCrn(offenderUpdate.getCrn());

        final var offenderToUpdate = dbOffender.map(oe ->
            oe.withProbationStatus(offenderUpdate.getProbationStatus())
                .withPreviouslyKnownTerminationDate(offenderUpdate.getPreviouslyKnownTerminationDate())
                .withSuspendedSentenceOrder(offenderUpdate.isSuspendedSentenceOrder())
                .withBreach(offenderUpdate.isBreach())
                .withPreSentenceActivity(offenderUpdate.isPreSentenceActivity())
                .withAwaitingPsr(offenderUpdate.getAwaitingPsr()))
            .orElse(offenderUpdate);

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
