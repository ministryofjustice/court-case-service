package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantOffender;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderUpdateService {

    private final DefendantRepository defendantRepository;
    private final OffenderRepository offenderRepository;

    public OffenderUpdateService(DefendantRepository defendantRepository, OffenderRepository offenderRepository) {
        this.defendantRepository = defendantRepository;
        this.offenderRepository = offenderRepository;
    }

    public void removeDefendantOffenderAssociation(final String defendantId) {
        final var defendant = findDefendantOrElseThrow(defendantId);

        if (!StringUtils.isEmpty(defendant.getCrn())) {
            defendantRepository.save(defendant.withCrn(null));
        }
    }

    public Mono<DefendantOffender> getDefendantOffenderByDefendantId(final String defendantId) {

        final var defendant = findDefendantOrElseThrow(defendantId);

        final var offender =  StringUtils.isEmpty(defendant.getCrn()) ? DefendantOffender.builder().build() :
                offenderRepository.findByCrn(defendant.getCrn()).map(DefendantOffender::of).orElse(DefendantOffender.builder().build());

        return Mono.just(offender);
    }

    public Mono<DefendantOffender> updateDefendantOffender(final String defendantId, OffenderEntity offenderUpdate) {

        final var defendant = findDefendantOrElseThrow(defendantId);

        final var dbOffender = offenderRepository.findByCrn(offenderUpdate.getCrn());

        final var offenderToUpdate = dbOffender.map(oe -> {
            oe.setProbationStatus(offenderUpdate.getProbationStatus());
            oe.setPreviouslyKnownTerminationDate(offenderUpdate.getPreviouslyKnownTerminationDate());
            oe.setSuspendedSentenceOrder(offenderUpdate.isSuspendedSentenceOrder());
            oe.setBreach(offenderUpdate.isBreach());
            oe.setPreSentenceActivity(offenderUpdate.isPreSentenceActivity());
            oe.setAwaitingPsr(offenderUpdate.getAwaitingPsr());
            return oe;
        }).orElse(offenderUpdate);

        final var updatedOffender = offenderRepository.save(offenderToUpdate);

        if (!StringUtils.equals(defendant.getCrn(), offenderToUpdate.getCrn())) {
            final var updatedDefendant = defendant.withCrn(offenderUpdate.getCrn());
            defendantRepository.save(updatedDefendant);
        }

        return Mono.just(DefendantOffender.of(updatedOffender));
    }

    private DefendantEntity findDefendantOrElseThrow(String defendantId) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(defendantId)
                .orElseThrow(() -> new EntityNotFoundException("Defendant with id %s does not exist", defendantId));
    }
}
