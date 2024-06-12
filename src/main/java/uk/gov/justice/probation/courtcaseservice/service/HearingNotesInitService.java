package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;

import java.util.Optional;

@Service
public class HearingNotesInitService {

    private final HearingRepository hearingRepository;

    public HearingNotesInitService(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    @Transactional
    public Optional<HearingEntity> initializeNote(String hearingId) {
        var hearing = hearingRepository.findFirstByHearingId(hearingId);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants().getFirst().getNotes());
            Hibernate.initialize(hearing.get().getHearingDefendants().stream().map(hearingDefendantEntity -> {
                // something up with judicial results here
                return hearingDefendantEntity.getOffences().stream().map(OffenceEntity::getJudicialResults);
            }));
        }
        return hearing;
    }
}
