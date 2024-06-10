package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;

import java.util.Optional;

@Service
public class HearingNotesServiceInitService {

    private final HearingRepository hearingRepository;

    public HearingNotesServiceInitService(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    @Transactional
    public Optional<HearingEntity> initializeNote(String hearingId) {
        var hearing = hearingRepository.findFirstByHearingId(hearingId);

//        Hibernate.initialize(hearing.map(hearingEntity -> hearingEntity.getHearingDefendants().getFirst().getNotes()));
        Hibernate.initialize(hearing.get().getHearingDefendants().getFirst().getNotes());

        return hearing;
    }
}
