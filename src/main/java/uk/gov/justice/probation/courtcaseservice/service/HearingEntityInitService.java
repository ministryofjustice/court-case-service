package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import java.util.List;
import java.util.Optional;

@Service
public class HearingEntityInitService {

    private final HearingRepository hearingRepository;

    public HearingEntityInitService(HearingRepository hearingRepository) {
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

    @Transactional
    public Optional<HearingEntity> findByCourtCodeCaseNoAndListNo(String courtCode, String caseNo, String listNo) {
        var hearing = hearingRepository.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, listNo);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants().getFirst().getNotes());
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
            hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
            Hibernate.initialize(hearing.get().getHearingDefendants().stream().map(hearingDefendantEntity ->
                    hearingDefendantEntity.getOffences().stream().map(OffenceEntity::getJudicialResults)));
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findMostRecentByCourtCodeAndCaseNo(String courtCode, String caseNo) {
        var hearing = hearingRepository.findMostRecentByCourtCodeAndCaseNo(courtCode, caseNo);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants().getFirst().getNotes());
            Hibernate.initialize(hearing.get().getHearingDefendants().stream().map(hearingDefendantEntity -> {
                // something up with judicial results here
                return hearingDefendantEntity.getOffences().stream().map(OffenceEntity::getJudicialResults);
            }));
        }
        return hearing;
    }

    @Transactional
    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {
        var hearings = hearingRepository.filterHearings(hearingSearchFilter);
        return hearings.stream().peek(hearingEntity -> {
            Hibernate.initialize(hearingEntity.getHearingDefendants().getFirst().getNotes());
            Hibernate.initialize(hearingEntity.getHearingDefendants().stream().map(hearingDefendantEntity -> {
                return hearingDefendantEntity.getOffences().stream().map(OffenceEntity::getJudicialResults);
            }));
        }).toList();
    }
}
