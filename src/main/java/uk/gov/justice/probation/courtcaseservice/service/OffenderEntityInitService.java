package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;

import java.util.Optional;

@Slf4j
@Service
public class OffenderEntityInitService {

    private final OffenderRepository offenderRepository;

    public OffenderEntityInitService(OffenderRepository offenderRepository) {
        this.offenderRepository = offenderRepository;
    }

    @Transactional
    public Optional<OffenderEntity> findByCrn(String crn) {
        var offender = offenderRepository.findByCrn(crn);
        // Hibernate initialize seems to have issues if mapping over an optional
        offender.ifPresent(o -> {
            Hibernate.initialize(offender.get().getDefendants());
            if (o.getDefendants() != null) {
                o.getDefendants().forEach(defendantEntity -> Hibernate.initialize(defendantEntity.getHearingDefendants()));
            } else {
                log.error("Offender with crn {} has no defendants", crn);
            }
        });
        return offender;
    }
}
