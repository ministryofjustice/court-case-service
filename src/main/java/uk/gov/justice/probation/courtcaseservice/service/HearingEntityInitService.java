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
        var hearing = hearingRepository.findFirstByHearingIdOrderByCreatedDesc(hearingId);
        return getHearingEntity(hearing);
    }

    @Transactional
    public Optional<HearingEntity> findFirstByHearingIdCourtCaseId(String hearingId, String courtCaseId) {
        var hearing = hearingRepository.findByHearingIdAndCaseId(hearingId, courtCaseId);
        return getHearingEntity(hearing);
    }

    @NotNull
    private Optional<HearingEntity> getHearingEntity(Optional<HearingEntity> hearing) {
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> {
                        hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()));
                        Hibernate.initialize(hearingDefendantEntity.getDefendant().getHearingDefendants());
                        if(hearingDefendantEntity.getDefendant().getOffender() != null) {
                            Hibernate.initialize(hearingDefendantEntity.getDefendant().getOffender().getDefendants());
                        }
                    }
            );
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findByHearingIdAndInitHearingDefendants(String hearingId, String defendantId) {
        var hearing = hearingRepository.findByHearingIdAndHearingDefendantsDefendantIdAndDeletedFalse(hearingId, defendantId);
        hearing.ifPresent(hearingEntity -> Hibernate.initialize(hearingEntity.getHearingDefendants()));
        return hearing;
    }

    @Transactional
    //TODO this is only used in tests - need to revisit this and think of a better way
    public Optional<HearingEntity> findByIdAndInitHearingDefendants(Long id) {
        var hearing = hearingRepository.findById(id);
        hearing.ifPresent(hearingEntity -> Hibernate.initialize(hearingEntity.getHearingDefendants()));
        Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
        hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findByHearingIdAndInitHearingNotes(String hearingId, String defendantId) {
        var hearing = hearingRepository.findByHearingIdAndHearingDefendantsDefendantIdAndDeletedFalse(hearingId, defendantId);
        if(hearing.isPresent()) {
            Hibernate.initialize(hearing.get().getHearingDefendants());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> Hibernate.initialize(hearingDefendantEntity.getNotes()));
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findFirstByHearingIdFileUpload(String hearingId, String defendantId) {
        var hearing = hearingRepository.findByHearingIdAndHearingDefendantsDefendantIdAndDeletedFalse(hearingId, defendantId);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
            hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findHearingByHearingIdAndDefendantIdInitialiseCaseDefendants(String hearingId, String defendantId) {
        var hearing = hearingRepository.findByHearingIdAndHearingDefendantsDefendantIdAndDeletedFalse(hearingId, defendantId);

        if(hearing.isPresent()) {
            HearingEntity hearingEntity = hearing.get();
            Hibernate.initialize(hearingEntity.getCourtCase().getCaseDefendants());
            hearingEntity.getCourtCase().getCaseDefendant(defendantId)
                .map (caseDefendantEntity -> {
                            Hibernate.initialize(caseDefendantEntity.getDocuments());
                            return caseDefendantEntity.getDocuments();
                        }
                );
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findByHearingIdAndDefendantIdAssignState(String hearingId, String defendantId) {
        var hearing = hearingRepository.findByHearingIdAndHearingDefendantsDefendantIdAndDeletedFalse(hearingId, defendantId);
        hearing.ifPresent(hearingEntity -> Hibernate.initialize(hearingEntity.getHearingDefendants()));
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findByCourtCodeCaseNoAndListNo(String courtCode, String caseNo, String listNo) {
        var hearing = hearingRepository.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, listNo);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
            hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity ->
                    hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()))
            );
        }
        return hearing;
    }

    @Transactional
    public Optional<HearingEntity> findMostRecentByCourtCodeAndCaseNo(String courtCode, String caseNo) {
        var hearing = hearingRepository.findMostRecentByCourtCodeAndCaseNo(courtCode, caseNo);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
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

    @Transactional
    public Optional<HearingEntity> findFirstByHearingIdFullyInitialised(String hearingId) {
        var hearing = hearingRepository.findFirstByHearingIdOrderByCreatedDesc(hearingId);
        if(hearing.isPresent()) { //Hibernate initialize seems to have issues if mapping over an optional
            Hibernate.initialize(hearing.get().getHearingDefendants());
            Hibernate.initialize(hearing.get().getCourtCase().getCaseDefendants());
            hearing.get().getCourtCase().getCaseDefendants().forEach(caseDefendantEntity -> Hibernate.initialize(caseDefendantEntity.getDocuments()));
            Hibernate.initialize(hearing.get().getCourtCase().getHearings());
            hearing.get().getHearingDefendants().forEach(hearingDefendantEntity -> {
                        Hibernate.initialize(hearingDefendantEntity.getNotes());
                        Hibernate.initialize(hearingDefendantEntity.getDefendant().getHearingDefendants());
                        hearingDefendantEntity.getOffences().forEach(offence -> Hibernate.initialize(offence.getJudicialResults()));
                        if(hearingDefendantEntity.getDefendant().getOffender() != null) {
                            Hibernate.initialize(hearingDefendantEntity.getDefendant().getOffender().getDefendants());
                        }
                    }
            );
        }
        return hearing;
    }
}
