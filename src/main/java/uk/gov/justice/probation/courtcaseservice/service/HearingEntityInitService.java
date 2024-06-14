package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HearingEntityInitService {

    private final HearingRepository hearingRepository;

    public HearingEntityInitService(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    @Transactional
    public Optional<HearingEntity> findFirstByHearingId(String hearingId) {
        var hearing = hearingRepository.findFirstByHearingId(hearingId);
        return initializeHearingEntity(hearing);
    }

    @Transactional
    public Optional<HearingEntity> findFirstByHearingIdWithOffenders(String hearingId) {
        var hearing = hearingRepository.findFirstByHearingId(hearingId);
        hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> {
                    Hibernate.initialize(hearingDefendantEntity.getNotes());
                    Hibernate.initialize(hearingDefendantEntity.getDefendant().getOffender().getDefendants());
                }
        );
        initializeHearingEntity(hearing);
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findByCourtCodeCaseNoAndListNo(String courtCode, String caseNo, String listNo) {
        var hearing = hearingRepository.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, listNo);
        return initializeHearingEntity(hearing);
    }

    @NotNull
    private Optional<HearingEntity> initializeHearingEntity(Optional<HearingEntity> hearing) {
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> Hibernate.initialize(hearingDefendantEntity.getNotes()));
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
            hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
            Hibernate.initialize(hearing.get().getCourtCase().getHearings());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> {
                Hibernate.initialize(hearingDefendantEntity.getDefendant().getHearingDefendants());
                hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()));
            }
            );
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findMostRecentByCourtCodeAndCaseNo(String courtCode, String caseNo) {
        var hearing = hearingRepository.findMostRecentByCourtCodeAndCaseNo(courtCode, caseNo);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> Hibernate.initialize(hearingDefendantEntity.getNotes()));
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity ->
                    hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()))
            );
        }
        return hearing;
    }

    @Transactional
    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {
        var hearings = hearingRepository.filterHearings(hearingSearchFilter);
        return hearings.stream().peek(hearingEntity -> {
            Hibernate.initialize(hearingEntity.getHearingDefendants());
            hearingEntity.getHearingDefendants().forEach(hearingDefendantEntity -> Hibernate.initialize(hearingDefendantEntity.getNotes()));
            hearingEntity.getHearingDefendants().forEach(hearingDefendantEntity ->
                    hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()))
            );
        }).toList();
    }

    @Transactional
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay) {
        var hearings = hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay);
        return hearings.stream().peek(hearingEntity -> {
            Hibernate.initialize(hearingEntity.getHearingDefendants());
        }).toList();
    }

    @Transactional
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        var hearings = hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore);
        return hearings.stream().peek(hearingEntity -> {
            Hibernate.initialize(hearingEntity.getHearingDefendants());
        }).toList();
    }
}
