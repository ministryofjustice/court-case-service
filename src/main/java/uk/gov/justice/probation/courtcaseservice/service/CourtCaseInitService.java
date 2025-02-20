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
        //Hibernate initialize seems to have issues if mapping over an optional
        hearing.ifPresent(hearingEntity -> Hibernate.initialize(hearingEntity.getHearingDefendants().getFirst().getNotes()));
        return hearing;
    }
}
