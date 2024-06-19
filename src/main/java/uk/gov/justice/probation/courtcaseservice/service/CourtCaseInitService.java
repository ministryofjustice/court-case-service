package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.util.Optional;

@Service
public class CourtCaseInitService {

    private final HearingRepositoryFacade hearingRepositoryFacade;

    public CourtCaseInitService(HearingRepositoryFacade courtCaseRepository) {
        this.hearingRepositoryFacade = courtCaseRepository;
    }

    @Transactional
    public Optional<HearingEntity> initializeHearing(String hearingId) {
        var hearing = hearingRepositoryFacade.findFirstByHearingId(hearingId);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants().getFirst().getNotes());
        }
        return hearing;
    }
}
