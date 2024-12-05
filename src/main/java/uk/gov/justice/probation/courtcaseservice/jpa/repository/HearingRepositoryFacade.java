package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.HearingEntityInitService;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
/**
 * An intermediary between the Service layer and the JPA repository intended to present a clean interface
 * to Service layer consumers, abstracting away custom code required for maintaining immutable records.
 */
public class HearingRepositoryFacade {
    private static final int MAX_YEAR_SUPPORTED_BY_DB = 294276;
    private static final int MIN_YEAR_SUPPORTED_BY_DB = -4712;

    private final OffenderRepository offenderRepository;
    private final OffenderRepositoryFacade offenderRepositoryFacade;
    private final HearingRepository hearingRepository;

    private final HearingEntityInitService hearingEntityInitService;
    private final DefendantRepository defendantRepository;
    private final CaseCommentsRepository caseCommentsRepository;

    @Autowired
    public HearingRepositoryFacade(OffenderRepository offenderRepository, OffenderRepositoryFacade offenderRepositoryFacade,
                                   HearingRepository hearingRepository, HearingEntityInitService hearingEntityInitService, DefendantRepository defendantRepository,
                                   CaseCommentsRepository caseCommentsRepository) {
        this.offenderRepository = offenderRepository;
        this.offenderRepositoryFacade = offenderRepositoryFacade;
        this.hearingRepository = hearingRepository;
        this.hearingEntityInitService = hearingEntityInitService;
        this.defendantRepository = defendantRepository;
        this.caseCommentsRepository = caseCommentsRepository;
    }

    public Optional<HearingEntity> findFirstByHearingId(String hearingId) {
        return hearingEntityInitService.findFirstByHearingId(hearingId);
    }

    public Optional<HearingEntity> findFirstByHearingIdAndCourtCaseId(String hearingId, String courtCaseId) {
        return hearingEntityInitService.findFirstByHearingIdCourtCaseId(hearingId, courtCaseId);
    }

    public Optional<HearingEntity> findFirstByHearingIdFileUpload(String hearingId, String defendantId) {
        return hearingEntityInitService.findFirstByHearingIdFileUpload(hearingId, defendantId);
    }

    public Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo, String listNo) {
        Optional<HearingEntity> hearing;
        if(StringUtils.isEmpty(listNo)) {
            hearing = hearingEntityInitService.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, null);
        } else {
            hearing = hearingEntityInitService.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, listNo)
                .or(() -> hearingEntityInitService.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, null));
        }
        return hearing
            .or(() -> hearingEntityInitService.findMostRecentByCourtCodeAndCaseNo(courtCode, caseNo).map(hearingEntity -> hearingEntity.withHearingId(null)));
    }

    public Optional<HearingEntity> findByHearingIdAndDefendantId(String hearingId, String defendantId) {
        return hearingEntityInitService.findFirstByHearingId(hearingId)
            .map(hearingEntity -> Objects.nonNull(hearingEntity.getHearingDefendant(defendantId)) ? hearingEntity : null)
            .map(hearingEntity -> {
                hearingEntity.getCourtCase().setCaseComments(caseCommentsRepository.findByCaseIdAndDefendantIdAndDeletedFalse(hearingEntity.getCaseId(), defendantId));
                return hearingEntity;
            });
    }

    public Optional<HearingEntity> findHearingByHearingIdAndDefendantIdInitialiseCaseDefendants(String hearingId, String defendantId) {
        return hearingEntityInitService.findHearingByHearingIdAndDefendantIdInitialiseCaseDefendants(hearingId, defendantId)
            .map(hearingEntity -> Objects.nonNull(hearingEntity.getHearingDefendant(defendantId)) ? hearingEntity : null)
            .map(hearingEntity -> {
                hearingEntity.getCourtCase().setCaseComments(caseCommentsRepository.findByCaseIdAndDefendantIdAndDeletedFalse(hearingEntity.getCaseId(), defendantId));
                return hearingEntity;
            });
    }

    @Deprecated
    /**
     * @deprecated Deprecated in favour of the version without createdAfter and createdBefore parameters as the lookup is
     * significantly more efficient without these constraints.
     */
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {

        return canIgnoreCreatedDates(createdAfter, createdBefore)
            ? hearingEntityInitService.findByCourtCodeAndHearingDay(courtCode, hearingDay)
            : hearingEntityInitService.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore);
    }

    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingEntityInitService.findByCourtCodeAndHearingDay(courtCode, hearingDay);
    }

    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, hearingDay);
    }

    public HearingEntity save(HearingEntity hearingEntity) {

        updateWithExistingOffenders(hearingEntity);

        updatedWithExistingDefendantsFromDb(hearingEntity);

        return hearingRepository.save(hearingEntity);
    }

    public List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter) {
        return hearingEntityInitService.filterHearings(hearingSearchFilter);
    }

    private void updateWithExistingOffenders(HearingEntity hearingEntity) {
        hearingEntity.getHearingDefendants().stream().filter(hearingDefendantEntity -> {
                    return Objects.nonNull(hearingDefendantEntity.getDefendant().getOffender()) &&
                                    Objects.isNull(hearingDefendantEntity.getDefendant().getOffender().getId());
                        })
            .forEach((HearingDefendantEntity hearingDefendantEntity) -> {
                var defendant = hearingDefendantEntity.getDefendant();
                var updatedOffender = offenderRepositoryFacade.upsertOffender(defendant.getOffender());
                defendant.setOffender(updatedOffender);
        });
    }

    private void updatedWithExistingDefendantsFromDb(HearingEntity hearingEntity) {
        // Check if incoming defendant already exists in the database and update
        hearingEntity.getHearingDefendants().stream()
            .filter(hearingDefendant -> Objects.isNull(hearingDefendant.getDefendant().getId())) // ID not null means this defendant has already been fetched and updated
            .forEach(defendantUpdate -> defendantRepository.findFirstByDefendantId(defendantUpdate.getDefendantId())
                .ifPresent(dbDefendant -> {
                    dbDefendant.update(defendantUpdate.getDefendant());
                    defendantUpdate.setDefendant(dbDefendant);
                }));
    }

    private boolean canIgnoreCreatedDates(LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return (createdAfter == null && createdBefore == null)
                || (createdAfter.getYear() <= MIN_YEAR_SUPPORTED_BY_DB && createdBefore.getYear() >= MAX_YEAR_SUPPORTED_BY_DB);
    }

    private Optional<HearingDefendantEntity> findDefendant(HearingEntity hearingEntity, String defendantId) {
        return hearingEntity.getHearingDefendants()
                .stream()
                .filter(hearingDefendantEntity -> defendantId.equals(hearingDefendantEntity.getDefendantId()))
                .findFirst();
    }


    public Optional<HearingEntity> findById(Long id) {
        return hearingEntityInitService.findByIdAndInitHearingDefendants(id);
    }

    public void deleteHearing(Long hearingDbId) {
        try {
            hearingRepository.deleteById(hearingDbId);
        } catch (Exception e) {
            log.error("Error deleting hearing with id {}", hearingDbId, e);
        }
    }
}
