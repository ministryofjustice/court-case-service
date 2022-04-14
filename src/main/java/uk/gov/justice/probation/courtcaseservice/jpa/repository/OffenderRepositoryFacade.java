package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

@Repository
@Transactional
public class OffenderRepositoryFacade {

    private final OffenderRepository offenderRepository;

    @Autowired
    public OffenderRepositoryFacade(OffenderRepository offenderRepository) {
        this.offenderRepository = offenderRepository;
    }

    public OffenderEntity updateOffenderIfItExists(OffenderEntity updatedOffender) {
        return offenderRepository.findByCrn(updatedOffender.getCrn())
            .map(existingOffender -> {
                // Implementation note: We're breaking immutability here because otherwise Hibernate will
                // encounter an exception 'Row was updated or deleted by another transaction (or unsaved-value
                // mapping was incorrect)' due a conflict between the existing record and the update.
                existingOffender.setBreach(updatedOffender.isBreach());
                existingOffender.setAwaitingPsr(updatedOffender.getAwaitingPsr());
                existingOffender.setProbationStatus(updatedOffender.getProbationStatus());
                existingOffender.setPreviouslyKnownTerminationDate(updatedOffender.getPreviouslyKnownTerminationDate());
                existingOffender.setSuspendedSentenceOrder(updatedOffender.isSuspendedSentenceOrder());
                existingOffender.setPreSentenceActivity(updatedOffender.isPreSentenceActivity());
                return existingOffender;
            })
            .orElse(updatedOffender);
    }

}
