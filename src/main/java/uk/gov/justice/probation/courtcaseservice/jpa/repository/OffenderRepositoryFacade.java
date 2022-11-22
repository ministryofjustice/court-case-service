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

    public OffenderEntity save(OffenderEntity offenderUpdate) {
        final var offenderToUpdate = updateOffenderIfItExists(offenderUpdate);

        return offenderRepository.save(offenderToUpdate);
    }

    public OffenderEntity updateOffenderIfItExists(OffenderEntity updatedOffender) {
        return offenderRepository.findByCrn(updatedOffender.getCrn())
            .map(existingOffender -> {
                existingOffender.update(updatedOffender);
                return existingOffender;
            })
            .orElse(updatedOffender);
    }

    public OffenderEntity upsertOffender(OffenderEntity updatedOffender) {
        return offenderRepository.findByCrn(updatedOffender.getCrn())
            .map(existingOffender -> {
                if(!existingOffender.withId(null).equals(updatedOffender)) {
                    existingOffender.update(updatedOffender);
                    return offenderRepository.save(existingOffender);
                }
                return existingOffender;
            })
            .orElseGet(() -> offenderRepository.save(updatedOffender));
    }
}
